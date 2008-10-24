/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.awt.Color;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationAdminException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.ApplicationServiceRemote;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AllowedDeviceDAO;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.ApplicationScopeIdDAO;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.model.ApplicationIdentityManager;
import net.link.safeonline.model.ApplicationOwnerManager;
import net.link.safeonline.model.Applications;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.notification.dao.EndpointReferenceDAO;
import net.link.safeonline.pkix.PkiUtils;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Implementation of application service interface.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class ApplicationServiceBean implements ApplicationService, ApplicationServiceRemote {

    private static final Log           LOG = LogFactory.getLog(ApplicationServiceBean.class);

    @EJB
    private ApplicationDAO             applicationDAO;

    @EJB
    private SubscriptionDAO            subscriptionDAO;

    @EJB
    private ApplicationScopeIdDAO      applicationScopeIdDAO;

    @EJB
    private SubjectService             subjectService;

    @EJB
    private ApplicationOwnerDAO        applicationOwnerDAO;

    @EJB
    private ApplicationOwnerManager    applicationOwnerManager;

    @EJB
    private AttributeTypeDAO           attributeTypeDAO;

    @EJB
    private ApplicationIdentityDAO     applicationIdentityDAO;

    @EJB
    private AttributeProviderDAO       attributeProviderDAO;

    @EJB
    private StatisticDAO               statisticDAO;

    @EJB
    private UsageAgreementDAO          usageAgreementDAO;

    @EJB
    private AllowedDeviceDAO           allowedDeviceDAO;

    @EJB
    private EndpointReferenceDAO       endpointReferenceDAO;

    @EJB
    private ApplicationIdentityManager applicationIdentityService;

    @EJB
    private Applications               applications;

    @EJB
    private SubjectManager             subjectManager;

    @Resource
    private SessionContext             sessionContext;


    @PermitAll
    public List<ApplicationEntity> listApplications() {

        if (this.sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE)
                || this.sessionContext.isCallerInRole(SafeOnlineRoles.OWNER_ROLE)
                || this.sessionContext.isCallerInRole(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE))
            return this.applications.listApplications();
        return this.applications.listUserApplications();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void addApplication(String name, String friendlyName, String applicationOwnerName, String description,
                               boolean idMappingServiceAccess, IdScopeType idScope, URL applicationUrl, byte[] applicationLogo,
                               Color applicationColor, byte[] encodedCertificate,
                               List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes, boolean skipMessageIntegrityCheck,
                               boolean deviceRestriction, boolean ssoEnabled, URL ssoLogoutUrl) throws ExistingApplicationException,
                                                                                               ApplicationOwnerNotFoundException,
                                                                                               CertificateEncodingException,
                                                                                               AttributeTypeNotFoundException {

        LOG.debug("add application: " + name);
        checkExistingApplication(name);

        X509Certificate certificate = PkiUtils.decodeCertificate(encodedCertificate);

        ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO.getApplicationOwner(applicationOwnerName);

        ApplicationEntity application = this.applicationDAO.addApplication(name, friendlyName, applicationOwner, description,
                applicationUrl, applicationLogo, applicationColor, certificate);

        application.setIdentifierMappingAllowed(idMappingServiceAccess);

        application.setIdScope(idScope);

        application.setSkipMessageIntegrityCheck(skipMessageIntegrityCheck);

        application.setDeviceRestriction(deviceRestriction);

        application.setSsoEnabled(ssoEnabled);

        application.setSsoLogoutUrl(ssoLogoutUrl);

        setInitialApplicationIdentity(initialApplicationIdentityAttributes, application);
    }

    private void setInitialApplicationIdentity(List<IdentityAttributeTypeDO> initialApplicationIdentityAttributeTypes,
                                               ApplicationEntity application) throws AttributeTypeNotFoundException {

        long initialIdentityVersion = ApplicationIdentityPK.INITIAL_IDENTITY_VERSION;
        ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO.addApplicationIdentity(application,
                initialIdentityVersion);
        application.setCurrentApplicationIdentity(initialIdentityVersion);

        addIdentityAttributes(applicationIdentity, initialApplicationIdentityAttributeTypes);
    }

    private void addIdentityAttributes(ApplicationIdentityEntity applicationIdentity,
                                       List<IdentityAttributeTypeDO> applicationIdentityAttributes) throws AttributeTypeNotFoundException {

        if (null == applicationIdentityAttributes)
            return;
        for (IdentityAttributeTypeDO identityAttribute : applicationIdentityAttributes) {
            AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(identityAttribute.getName());
            this.applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, attributeType, identityAttribute.isRequired(),
                    identityAttribute.isDataMining());
        }
    }

    private void checkExistingApplication(String name) throws ExistingApplicationException {

        ApplicationEntity existingApplication = this.applicationDAO.findApplication(name);
        if (null != existingApplication) {
            throw new ExistingApplicationException();
        }
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void removeApplication(String name) throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("remove application: " + name);
        ApplicationEntity application = this.applicationDAO.getApplication(name);

        if (false == application.isRemovable()) {
            throw new PermissionDeniedException("application not removable", "errorPermissionApplicationNotRemovable");
        }

        List<SubscriptionEntity> subscriptions = this.subscriptionDAO.listSubscriptions(application);

        /*
         * We don't rely on hibernate here to cascade remove the subscriptions and application identities for the moment. Postpone this
         * until be understand better what data needs to be preserved.
         */
        for (SubscriptionEntity subscription : subscriptions) {
            this.subscriptionDAO.removeSubscription(subscription);
        }

        this.applicationScopeIdDAO.removeApplicationScopeIds(application);

        List<ApplicationIdentityEntity> applicationIdentities = this.applicationIdentityDAO.listApplicationIdentities(application);
        for (ApplicationIdentityEntity applicationIdentity : applicationIdentities) {
            this.applicationIdentityDAO.removeApplicationIdentity(applicationIdentity);
        }

        // remove all device notification subscriptions
        List<EndpointReferenceEntity> endpoints = this.endpointReferenceDAO.listEndpoints(application);
        for (EndpointReferenceEntity endpoint : endpoints) {
            this.endpointReferenceDAO.remove(endpoint);
        }

        this.attributeProviderDAO.removeAttributeProviders(application);

        this.statisticDAO.removeStatistics(application);

        this.usageAgreementDAO.removeUsageAgreements(application);

        this.allowedDeviceDAO.deleteAllowedDevices(application);

        this.applicationOwnerDAO.removeApplication(application);

        this.applicationDAO.removeApplication(application);
    }

    /**
     * Check write permission on the given application. Only the subject corresponding with the application owner of the application is
     * allowed to write to the application entity.
     * 
     * @param application
     * @throws PermissionDeniedException
     */
    private void checkWritePermission(ApplicationEntity application) throws PermissionDeniedException {

        if (this.sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE))
            return;
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        SubjectEntity requiredSubject = applicationOwner.getAdmin();
        SubjectEntity actualSubject = this.subjectManager.getCallerSubject();
        if (false == requiredSubject.equals(actualSubject)) {
            throw new PermissionDeniedException("application owner admin mismatch");
        }
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void setApplicationDescription(String name, String description) throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("set application description: " + name);
        ApplicationEntity application = this.applicationDAO.getApplication(name);

        checkWritePermission(application);

        application.setDescription(description);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void registerApplicationOwner(String ownerName, String adminLogin) throws SubjectNotFoundException,
                                                                             ExistingApplicationOwnerException,
                                                                             ExistingApplicationAdminException {

        LOG.debug("register application owner: " + ownerName + " with account " + adminLogin);
        checkExistingOwner(ownerName);

        SubjectEntity adminSubject = this.subjectService.getSubjectFromUserName(adminLogin);
        checkExistingAdmin(adminSubject);

        this.applicationOwnerDAO.addApplicationOwner(ownerName, adminSubject);

        ApplicationEntity ownerApplication = this.applicationDAO.findApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
        if (null == ownerApplication) {
            throw new EJBException("SafeOnline owner application not found");
        }

        /*
         * Subscribe the new application owner to the SafeOnline owner web application so he can do it's job.
         */
        this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION, adminSubject, ownerApplication);

        /*
         * We have to flush the credential cache for the login here. Else it's possible that the login cannot logon because JAAS is caching
         * the old roles that did not include the 'owner' role yet.
         */
        SecurityManagerUtils.flushCredentialCache(adminSubject.getUserId(), SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
    }

    private void checkExistingAdmin(SubjectEntity adminSubject) throws ExistingApplicationAdminException {

        ApplicationOwnerEntity existingApplicationOwner = this.applicationOwnerDAO.findApplicationOwner(adminSubject);
        if (null != existingApplicationOwner) {
            throw new ExistingApplicationAdminException();
        }
    }

    private void checkExistingOwner(String name) throws ExistingApplicationOwnerException {

        ApplicationOwnerEntity existingApplicationOwner = this.applicationOwnerDAO.findApplicationOwner(name);
        if (null != existingApplicationOwner) {
            throw new ExistingApplicationOwnerException();
        }
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeApplicationOwner(String ownerName, String adminLogin) throws SubscriptionNotFoundException, SubjectNotFoundException,
                                                                           ApplicationOwnerNotFoundException, PermissionDeniedException {

        LOG.debug("remove application owner: " + ownerName);

        checkOwnerApplications(ownerName);

        SubjectEntity adminSubject = this.subjectService.getSubjectFromUserName(adminLogin);

        this.applicationOwnerDAO.removeApplicationOwner(ownerName);

        ApplicationEntity ownerApplication = this.applicationDAO.findApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
        if (null == ownerApplication) {
            throw new EJBException("SafeOnline owner application not found");
        }

        /*
         * Remove the application owner's subscription to the SafeOnline Owner web application.
         */
        this.subscriptionDAO.removeSubscription(adminSubject, ownerApplication);

        /*
         * Flush the credential cache as the owner role is no longer for this login.
         */
        SecurityManagerUtils.flushCredentialCache(adminSubject.getUserId(), SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
    }

    private void checkOwnerApplications(String name) throws ApplicationOwnerNotFoundException, PermissionDeniedException {

        ApplicationOwnerEntity owner = this.applicationOwnerDAO.getApplicationOwner(name);
        if (null == owner.getApplications())
            return;
        if (!owner.getApplications().isEmpty()) {
            throw new PermissionDeniedException("application owner still owns " + owner.getApplications().size() + " applications");
        }
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ApplicationOwnerEntity> listApplicationOwners() {

        LOG.debug("get application owners");
        List<ApplicationOwnerEntity> applicationOwners = this.applicationOwnerDAO.listApplicationOwners();
        return applicationOwners;
    }

    @RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
    public List<ApplicationEntity> getOwnedApplications() throws ApplicationOwnerNotFoundException {

        LOG.debug("get owned applications");
        ApplicationOwnerEntity applicationOwner = this.applicationOwnerManager.getCallerApplicationOwner();
        List<ApplicationEntity> tempApplications = this.applicationDAO.listApplications(applicationOwner);
        return tempApplications;
    }

    @RolesAllowed( { SafeOnlineRoles.OPERATOR_ROLE, SafeOnlineRoles.OWNER_ROLE })
    public Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(String applicationName)
                                                                                                        throws ApplicationNotFoundException,
                                                                                                        ApplicationIdentityNotFoundException,
                                                                                                        PermissionDeniedException {

        ApplicationEntity application = this.applications.getApplication(applicationName);

        checkReadPermission(application);

        return this.applications.getCurrentApplicationIdentity(application);
    }

    private void checkReadPermission(ApplicationEntity application) throws PermissionDeniedException {

        if (this.sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE))
            return;
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        SubjectEntity expectedSubject = applicationOwner.getAdmin();
        SubjectEntity actualSubject = this.subjectManager.getCallerSubject();
        if (false == expectedSubject.equals(actualSubject)) {
            throw new PermissionDeniedException("application owner admin mismatch");
        }
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationIdentity(String applicationId, List<IdentityAttributeTypeDO> applicationIdentityAttributes)
                                                                                                                            throws ApplicationNotFoundException,
                                                                                                                            ApplicationIdentityNotFoundException,
                                                                                                                            AttributeTypeNotFoundException {

        this.applicationIdentityService.updateApplicationIdentity(applicationId, applicationIdentityAttributes);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationUrl(String applicationId, URL applicationUrl) throws ApplicationNotFoundException {

        getApplication(applicationId).setApplicationUrl(applicationUrl);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationLogo(String applicationId, byte[] applicationLogo) throws ApplicationNotFoundException {

        getApplication(applicationId).setApplicationLogo(applicationLogo);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationColor(String applicationId, Color applicationColor) throws ApplicationNotFoundException {

        getApplication(applicationId).setApplicationColor(applicationColor);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public ApplicationEntity getApplication(String applicationName) throws ApplicationNotFoundException {

        return this.applicationDAO.getApplication(applicationName);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationCertificate(String applicationName, byte[] certificateData) throws CertificateEncodingException,
                                                                                            ApplicationNotFoundException {

        LOG.debug("updating application certificate for " + applicationName);
        X509Certificate certificate = PkiUtils.decodeCertificate(certificateData);

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);

        application.setCertificate(certificate);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void setApplicationDeviceRestriction(String name, boolean deviceRestriction) throws ApplicationNotFoundException,
                                                                                       PermissionDeniedException {

        LOG.debug("set application description: " + name);
        ApplicationEntity application = this.applicationDAO.getApplication(name);

        checkWritePermission(application);

        application.setDeviceRestriction(deviceRestriction);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setIdentifierMappingServiceAccess(String applicationName, boolean access) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        application.setIdentifierMappingAllowed(access);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setIdScope(String applicationName, IdScopeType idScope) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        application.setIdScope(idScope);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setSkipMessageIntegrityCheck(String applicationName, boolean skipMessageIntegrityCheck) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        application.setSkipMessageIntegrityCheck(skipMessageIntegrityCheck);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setSsoEnabled(String applicationName, boolean ssoEnabled) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        application.setSsoEnabled(ssoEnabled);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateSsoLogoutUrl(String applicationId, URL ssoLogoutUrl) throws ApplicationNotFoundException {

        getApplication(applicationId).setSsoLogoutUrl(ssoLogoutUrl);

    }
}
