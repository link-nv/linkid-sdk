/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Implementation of application service interface.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = ApplicationService.JNDI_BINDING)
@RemoteBinding(jndiBinding = ApplicationServiceRemote.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class ApplicationServiceBean implements ApplicationService, ApplicationServiceRemote {

    private static final Log           LOG = LogFactory.getLog(ApplicationServiceBean.class);

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO             applicationDAO;

    @EJB(mappedName = SubscriptionDAO.JNDI_BINDING)
    private SubscriptionDAO            subscriptionDAO;

    @EJB(mappedName = ApplicationScopeIdDAO.JNDI_BINDING)
    private ApplicationScopeIdDAO      applicationScopeIdDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService             subjectService;

    @EJB(mappedName = ApplicationOwnerDAO.JNDI_BINDING)
    private ApplicationOwnerDAO        applicationOwnerDAO;

    @EJB(mappedName = ApplicationOwnerManager.JNDI_BINDING)
    private ApplicationOwnerManager    applicationOwnerManager;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO           attributeTypeDAO;

    @EJB(mappedName = ApplicationIdentityDAO.JNDI_BINDING)
    private ApplicationIdentityDAO     applicationIdentityDAO;

    @EJB(mappedName = AttributeProviderDAO.JNDI_BINDING)
    private AttributeProviderDAO       attributeProviderDAO;

    @EJB(mappedName = StatisticDAO.JNDI_BINDING)
    private StatisticDAO               statisticDAO;

    @EJB(mappedName = UsageAgreementDAO.JNDI_BINDING)
    private UsageAgreementDAO          usageAgreementDAO;

    @EJB(mappedName = AllowedDeviceDAO.JNDI_BINDING)
    private AllowedDeviceDAO           allowedDeviceDAO;

    @EJB(mappedName = EndpointReferenceDAO.JNDI_BINDING)
    private EndpointReferenceDAO       endpointReferenceDAO;

    @EJB(mappedName = ApplicationIdentityManager.JNDI_BINDING)
    private ApplicationIdentityManager applicationIdentityService;

    @EJB(mappedName = Applications.JNDI_BINDING)
    private Applications               applications;

    @EJB(mappedName = SubjectManager.JNDI_BINDING)
    private SubjectManager             subjectManager;

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager              entityManager;

    @Resource
    private SessionContext             sessionContext;


    @PermitAll
    public List<ApplicationEntity> listApplications() {

        if (sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE) || sessionContext.isCallerInRole(SafeOnlineRoles.OWNER_ROLE)
                || sessionContext.isCallerInRole(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE))
            return applications.listApplications();
        return applications.listUserApplications();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ApplicationEntity addApplication(String name, String friendlyName, String applicationOwnerName, String description,
                                            boolean idMappingServiceAccess, IdScopeType idScope, URL applicationUrl,
                                            byte[] applicationLogo, byte[] encodedCertificate,
                                            List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes,
                                            boolean skipMessageIntegrityCheck, boolean deviceRestriction, boolean ssoEnabled,
                                            URL ssoLogoutUrl)
            throws ExistingApplicationException, ApplicationOwnerNotFoundException, CertificateEncodingException,
            AttributeTypeNotFoundException {

        LOG.debug("add application: " + name);
        checkExistingApplication(name);

        X509Certificate certificate = PkiUtils.decodeCertificate(encodedCertificate);

        ApplicationOwnerEntity applicationOwner = applicationOwnerDAO.getApplicationOwner(applicationOwnerName);

        ApplicationEntity application = applicationDAO.addApplication(name, friendlyName, applicationOwner, description, applicationUrl,
                applicationLogo, certificate);

        application.setIdentifierMappingAllowed(idMappingServiceAccess);

        application.setIdScope(idScope);

        application.setSkipMessageIntegrityCheck(skipMessageIntegrityCheck);

        application.setDeviceRestriction(deviceRestriction);

        application.setSsoEnabled(ssoEnabled);

        application.setSsoLogoutUrl(ssoLogoutUrl);

        setInitialApplicationIdentity(initialApplicationIdentityAttributes, application);

        return application;
    }

    private void setInitialApplicationIdentity(List<IdentityAttributeTypeDO> initialApplicationIdentityAttributeTypes,
                                               ApplicationEntity application)
            throws AttributeTypeNotFoundException {

        long initialIdentityVersion = ApplicationIdentityPK.INITIAL_IDENTITY_VERSION;
        ApplicationIdentityEntity applicationIdentity = applicationIdentityDAO.addApplicationIdentity(application, initialIdentityVersion);
        application.setCurrentApplicationIdentity(initialIdentityVersion);

        addIdentityAttributes(applicationIdentity, initialApplicationIdentityAttributeTypes);
    }

    private void addIdentityAttributes(ApplicationIdentityEntity applicationIdentity,
                                       List<IdentityAttributeTypeDO> applicationIdentityAttributes)
            throws AttributeTypeNotFoundException {

        if (null == applicationIdentityAttributes)
            return;
        for (IdentityAttributeTypeDO identityAttribute : applicationIdentityAttributes) {
            AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(identityAttribute.getName());
            applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, attributeType, identityAttribute.isRequired(),
                    identityAttribute.isDataMining());
        }
    }

    private void checkExistingApplication(String name)
            throws ExistingApplicationException {

        ApplicationEntity existingApplication = applicationDAO.findApplication(name);
        if (null != existingApplication)
            throw new ExistingApplicationException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void removeApplication(long applicationId)
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("remove application: " + applicationId);
        ApplicationEntity application = applicationDAO.getApplication(applicationId);

        if (false == application.isRemovable())
            throw new PermissionDeniedException("application not removable", "errorPermissionApplicationNotRemovable");

        List<SubscriptionEntity> subscriptions = subscriptionDAO.listSubscriptions(application);

        /*
         * We don't rely on hibernate here to cascade remove the subscriptions and application identities for the moment. Postpone this
         * until be understand better what data needs to be preserved.
         */
        for (SubscriptionEntity subscription : subscriptions) {
            subscriptionDAO.removeSubscription(subscription);
        }

        applicationScopeIdDAO.removeApplicationScopeIds(application);

        List<ApplicationIdentityEntity> applicationIdentities = applicationIdentityDAO.listApplicationIdentities(application);
        for (ApplicationIdentityEntity applicationIdentity : applicationIdentities) {
            applicationIdentityDAO.removeApplicationIdentity(applicationIdentity);
        }

        // remove all device notification subscriptions
        List<EndpointReferenceEntity> endpoints = endpointReferenceDAO.listEndpoints(application);
        for (EndpointReferenceEntity endpoint : endpoints) {
            endpointReferenceDAO.remove(endpoint);
        }

        attributeProviderDAO.removeAttributeProviders(application);

        statisticDAO.removeStatistics(application);

        usageAgreementDAO.removeUsageAgreements(application);

        allowedDeviceDAO.deleteAllowedDevices(application);

        applicationOwnerDAO.removeApplication(application);

        applicationDAO.removeApplication(application);
    }

    /**
     * Check write permission on the given application. Only the subject corresponding with the application owner of the application is
     * allowed to write to the application entity.
     * 
     * @param application
     * @throws PermissionDeniedException
     */
    private void checkWritePermission(ApplicationEntity application)
            throws PermissionDeniedException {

        if (sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE))
            return;
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        SubjectEntity requiredSubject = applicationOwner.getAdmin();
        SubjectEntity actualSubject = subjectManager.getCallerSubject();
        if (false == requiredSubject.equals(actualSubject))
            throw new PermissionDeniedException("application owner admin mismatch");
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void setApplicationDescription(long applicationId, String description)
            throws ApplicationNotFoundException, PermissionDeniedException {

        ApplicationEntity application = applicationDAO.getApplication(applicationId);

        checkWritePermission(application);

        application.setDescription(description);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void registerApplicationOwner(String ownerName, String adminLogin)
            throws SubjectNotFoundException, ExistingApplicationOwnerException, ExistingApplicationAdminException {

        LOG.debug("register application owner: " + ownerName + " with account " + adminLogin);
        checkExistingOwner(ownerName);

        SubjectEntity adminSubject = subjectService.getSubjectFromUserName(adminLogin);
        checkExistingAdmin(adminSubject);

        applicationOwnerDAO.addApplicationOwner(ownerName, adminSubject);

        ApplicationEntity ownerApplication = applicationDAO.findApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
        if (null == ownerApplication)
            throw new EJBException("SafeOnline owner application not found");

        /*
         * Subscribe the new application owner to the SafeOnline owner web application so he can do it's job.
         */
        subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION, adminSubject, ownerApplication);

        /*
         * We have to flush the credential cache for the login here. Else it's possible that the login cannot logon because JAAS is caching
         * the old roles that did not include the 'owner' role yet.
         */
        SecurityManagerUtils.flushCredentialCache(adminSubject.getUserId(), SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
    }

    private void checkExistingAdmin(SubjectEntity adminSubject)
            throws ExistingApplicationAdminException {

        ApplicationOwnerEntity existingApplicationOwner = applicationOwnerDAO.findApplicationOwner(adminSubject);
        if (null != existingApplicationOwner)
            throw new ExistingApplicationAdminException();
    }

    private void checkExistingOwner(String name)
            throws ExistingApplicationOwnerException {

        ApplicationOwnerEntity existingApplicationOwner = applicationOwnerDAO.findApplicationOwner(name);
        if (null != existingApplicationOwner)
            throw new ExistingApplicationOwnerException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeApplicationOwner(String ownerName, String adminLogin)
            throws SubscriptionNotFoundException, SubjectNotFoundException, ApplicationOwnerNotFoundException, PermissionDeniedException {

        LOG.debug("remove application owner: " + ownerName);

        checkOwnerApplications(ownerName);

        SubjectEntity adminSubject = subjectService.getSubjectFromUserName(adminLogin);

        applicationOwnerDAO.removeApplicationOwner(ownerName);

        ApplicationEntity ownerApplication = applicationDAO.findApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
        if (null == ownerApplication)
            throw new EJBException("SafeOnline owner application not found");

        /*
         * Remove the application owner's subscription to the SafeOnline Owner web application.
         */
        subscriptionDAO.removeSubscription(adminSubject, ownerApplication);

        /*
         * Flush the credential cache as the owner role is no longer for this login.
         */
        SecurityManagerUtils.flushCredentialCache(adminSubject.getUserId(), SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
    }

    private void checkOwnerApplications(String name)
            throws ApplicationOwnerNotFoundException, PermissionDeniedException {

        ApplicationOwnerEntity owner = applicationOwnerDAO.getApplicationOwner(name);
        if (null == owner.getApplications())
            return;
        if (!owner.getApplications().isEmpty())
            throw new PermissionDeniedException("application owner still owns " + owner.getApplications().size() + " applications");
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ApplicationOwnerEntity> listApplicationOwners() {

        LOG.debug("get application owners");
        List<ApplicationOwnerEntity> applicationOwners = applicationOwnerDAO.listApplicationOwners();
        return applicationOwners;
    }

    @RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
    public List<ApplicationEntity> getOwnedApplications()
            throws ApplicationOwnerNotFoundException {

        LOG.debug("get owned applications");
        ApplicationOwnerEntity applicationOwner = applicationOwnerManager.getCallerApplicationOwner();
        List<ApplicationEntity> tempApplications = applicationDAO.listApplications(applicationOwner);
        return tempApplications;
    }

    @RolesAllowed( { SafeOnlineRoles.OPERATOR_ROLE, SafeOnlineRoles.OWNER_ROLE })
    public Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(long applicationId)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException {

        ApplicationEntity application = applications.getApplication(applicationId);

        checkReadPermission(application);

        return applications.getCurrentApplicationIdentity(application);
    }

    private void checkReadPermission(ApplicationEntity application)
            throws PermissionDeniedException {

        if (sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE))
            return;
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        SubjectEntity expectedSubject = applicationOwner.getAdmin();
        SubjectEntity actualSubject = subjectManager.getCallerSubject();
        if (false == expectedSubject.equals(actualSubject))
            throw new PermissionDeniedException("application owner admin mismatch");
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationIdentity(long applicationId, List<IdentityAttributeTypeDO> applicationIdentityAttributes)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, AttributeTypeNotFoundException {

        applicationIdentityService.updateApplicationIdentity(applicationId, applicationIdentityAttributes);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationUrl(long applicationId, URL applicationUrl)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setApplicationUrl(applicationUrl);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationLogo(long applicationId, byte[] applicationLogo)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setApplicationLogo(applicationLogo);
        entityManager.flush(); // https://jira.jboss.org/jira/browse/JBPORTAL-983?focusedCommentId=12352050#action_12352050
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public ApplicationEntity getApplication(long applicationId)
            throws ApplicationNotFoundException {

        return applicationDAO.getApplication(applicationId);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public ApplicationEntity getApplication(String applicationName)
            throws ApplicationNotFoundException {

        return applicationDAO.getApplication(applicationName);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateApplicationCertificate(long applicationId, byte[] certificateData)
            throws CertificateEncodingException, ApplicationNotFoundException {

        LOG.debug("updating application certificate for " + applicationId);
        X509Certificate certificate = PkiUtils.decodeCertificate(certificateData);

        ApplicationEntity application = applicationDAO.getApplication(applicationId);

        application.setCertificate(certificate);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void setApplicationDeviceRestriction(long applicationId, boolean deviceRestriction)
            throws ApplicationNotFoundException, PermissionDeniedException {

        ApplicationEntity application = applicationDAO.getApplication(applicationId);

        checkWritePermission(application);

        application.setDeviceRestriction(deviceRestriction);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setIdentifierMappingServiceAccess(long applicationId, boolean access)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setIdentifierMappingAllowed(access);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setIdScope(long applicationId, IdScopeType idScope)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setIdScope(idScope);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setSkipMessageIntegrityCheck(long applicationId, boolean skipMessageIntegrityCheck)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setSkipMessageIntegrityCheck(skipMessageIntegrityCheck);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setSsoEnabled(long applicationId, boolean ssoEnabled)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setSsoEnabled(ssoEnabled);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateSsoLogoutUrl(long applicationId, URL ssoLogoutUrl)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setSsoLogoutUrl(ssoLogoutUrl);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed( { SafeOnlineRoles.OPERATOR_ROLE, SafeOnlineRoles.OWNER_ROLE })
    public void updateApplicationName(long applicationId, String applicationName)
            throws ApplicationNotFoundException, ExistingApplicationException {

        checkExistingApplication(applicationName);

        getApplication(applicationId).setName(applicationName);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed( { SafeOnlineRoles.OPERATOR_ROLE, SafeOnlineRoles.OWNER_ROLE })
    public void updateApplicationFriendlyName(long applicationId, String applicationFriendlyName)
            throws ApplicationNotFoundException {

        getApplication(applicationId).setFriendlyName(applicationFriendlyName);

    }
}
