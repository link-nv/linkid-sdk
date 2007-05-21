/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.ApplicationServiceRemote;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.ApplicationIdentityManager;
import net.link.safeonline.model.ApplicationOwnerManager;
import net.link.safeonline.model.Applications;
import net.link.safeonline.model.PkiUtils;
import net.link.safeonline.model.SubjectManager;
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
public class ApplicationServiceBean implements ApplicationService,
		ApplicationServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(ApplicationServiceBean.class);

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private SubjectDAO subjectDAO;

	@EJB
	private ApplicationOwnerDAO applicationOwnerDAO;

	@EJB
	private ApplicationOwnerManager applicationOwnerManager;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private ApplicationIdentityDAO applicationIdentityDAO;

	@EJB
	private AttributeProviderDAO attributeProviderDAO;

	@EJB
	private ApplicationIdentityManager applicationIdentityService;

	@EJB
	private Applications applications;

	@EJB
	private SubjectManager subjectManager;

	@Resource
	private SessionContext sessionContext;

	@PermitAll
	public List<ApplicationEntity> listApplications() {
		return this.applications.listApplications();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addApplication(String name, String applicationOwnerName,
			String description, byte[] encodedCertificate,
			List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes)
			throws ExistingApplicationException,
			ApplicationOwnerNotFoundException, CertificateEncodingException,
			AttributeTypeNotFoundException {
		LOG.debug("add application: " + name);
		checkExistingApplication(name);

		X509Certificate certificate = PkiUtils
				.decodeCertificate(encodedCertificate);

		ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
				.getApplicationOwner(applicationOwnerName);

		ApplicationEntity application = this.applicationDAO.addApplication(
				name, applicationOwner, description, certificate);

		setInitialApplicationIdentity(initialApplicationIdentityAttributes,
				application);
	}

	private void setInitialApplicationIdentity(
			List<IdentityAttributeTypeDO> initialApplicationIdentityAttributeTypes,
			ApplicationEntity application)
			throws AttributeTypeNotFoundException {
		long initialIdentityVersion = ApplicationIdentityPK.INITIAL_IDENTITY_VERSION;
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.addApplicationIdentity(application, initialIdentityVersion);
		application.setCurrentApplicationIdentity(initialIdentityVersion);

		addIdentityAttributes(applicationIdentity,
				initialApplicationIdentityAttributeTypes);
	}

	private void addIdentityAttributes(
			ApplicationIdentityEntity applicationIdentity,
			List<IdentityAttributeTypeDO> applicationIdentityAttributes)
			throws AttributeTypeNotFoundException {
		if (null == applicationIdentityAttributes) {
			return;
		}
		for (IdentityAttributeTypeDO identityAttribute : applicationIdentityAttributes) {
			AttributeTypeEntity attributeType = this.attributeTypeDAO
					.getAttributeType(identityAttribute.getName());
			this.applicationIdentityDAO.addApplicationIdentityAttribute(
					applicationIdentity, attributeType, identityAttribute
							.isRequired(), identityAttribute.isDataMining());
		}
	}

	private void checkExistingApplication(String name)
			throws ExistingApplicationException {
		ApplicationEntity existingApplication = this.applicationDAO
				.findApplication(name);
		if (null != existingApplication) {
			throw new ExistingApplicationException();
		}
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeApplication(String name)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("remove application: " + name);
		ApplicationEntity application = this.applicationDAO
				.getApplication(name);

		if (false == application.isRemovable()) {
			throw new PermissionDeniedException();
		}

		List<SubscriptionEntity> subscriptions = this.subscriptionDAO
				.listSubscriptions(application);
		/*
		 * We don't rely on hibernate here to cascade remove the subscriptions
		 * and application identities for the moment. Postpone this until be
		 * understand better what data needs to be preserved.
		 */
		for (SubscriptionEntity subscription : subscriptions) {
			this.subscriptionDAO.removeSubscription(subscription);
		}

		List<ApplicationIdentityEntity> applicationIdentities = this.applicationIdentityDAO
				.listApplicationIdentities(application);
		for (ApplicationIdentityEntity applicationIdentity : applicationIdentities) {
			this.applicationIdentityDAO
					.removeApplicationIdentity(applicationIdentity);
		}

		this.attributeProviderDAO.removeAttributeProviders(application);

		this.applicationDAO.removeApplication(application);
	}

	/**
	 * Check write permission on the given application. Only the subject
	 * corresponding with the application owner of the application is allowed to
	 * write to the application entity.
	 * 
	 * @param application
	 * @throws PermissionDeniedException
	 */
	public void checkWritePermission(ApplicationEntity application)
			throws PermissionDeniedException {
		ApplicationOwnerEntity applicationOwner = application
				.getApplicationOwner();
		SubjectEntity requiredSubject = applicationOwner.getAdmin();
		SubjectEntity actualSubject = this.subjectManager.getCallerSubject();
		if (false == requiredSubject.equals(actualSubject)) {
			throw new PermissionDeniedException();
		}
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public void setApplicationDescription(String name, String description)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("set application description: " + name);
		ApplicationEntity application = this.applicationDAO
				.getApplication(name);

		checkWritePermission(application);

		application.setDescription(description);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void registerApplicationOwner(String ownerName, String adminLogin)
			throws SubjectNotFoundException, ExistingApplicationOwnerException {
		LOG.debug("register application owner: " + ownerName + " with account "
				+ adminLogin);
		checkExistingOwner(ownerName);

		SubjectEntity adminSubject = this.subjectDAO.getSubject(adminLogin);

		this.applicationOwnerDAO.addApplicationOwner(ownerName, adminSubject);

		ApplicationEntity ownerApplication = this.applicationDAO
				.findApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);
		if (null == ownerApplication) {
			throw new EJBException("SafeOnline owner application not found");
		}

		/*
		 * Subscribe the new application owner to the SafeOnline owner web
		 * application so he can do it's job.
		 */
		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION,
				adminSubject, ownerApplication);

		/*
		 * We have to flush the credential cache for the login here. Else it's
		 * possible that the login cannot logon because JAAS is caching the old
		 * roles that did not include the 'owner' role yet.
		 */
		SecurityManagerUtils.flushCredentialCache(adminLogin,
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}

	private void checkExistingOwner(String name)
			throws ExistingApplicationOwnerException {
		ApplicationOwnerEntity existingApplicationOwner = this.applicationOwnerDAO
				.findApplicationOwner(name);
		if (null != existingApplicationOwner) {
			throw new ExistingApplicationOwnerException();
		}
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<ApplicationOwnerEntity> listApplicationOwners() {
		LOG.debug("get application owners");
		List<ApplicationOwnerEntity> applicationOwners = this.applicationOwnerDAO
				.listApplicationOwners();
		return applicationOwners;
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public List<ApplicationEntity> getOwnedApplications() {
		LOG.debug("get owned applications");
		ApplicationOwnerEntity applicationOwner = this.applicationOwnerManager
				.getCallerApplicationOwner();
		List<ApplicationEntity> applications = this.applicationDAO
				.listApplications(applicationOwner);
		return applications;
	}

	@RolesAllowed( { SafeOnlineRoles.OPERATOR_ROLE, SafeOnlineRoles.OWNER_ROLE })
	public List<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(
			String applicationName) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException, PermissionDeniedException {

		ApplicationEntity application = this.applications
				.getApplication(applicationName);

		checkReadPermission(application);

		return this.applications.getCurrentApplicationIdentity(application);
	}

	private void checkReadPermission(ApplicationEntity application)
			throws PermissionDeniedException {
		if (this.sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE)) {
			return;
		}
		ApplicationOwnerEntity applicationOwner = application
				.getApplicationOwner();
		SubjectEntity expectedSubject = applicationOwner.getAdmin();
		SubjectEntity actualSubject = this.subjectManager.getCallerSubject();
		if (false == expectedSubject.equals(actualSubject)) {
			throw new PermissionDeniedException();
		}
	}

	@SuppressWarnings("unchecked")
	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void updateApplicationIdentity(String applicationId,
			List<IdentityAttributeTypeDO> applicationIdentityAttributes)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException,
			AttributeTypeNotFoundException {
		this.applicationIdentityService.updateApplicationIdentity(
				applicationId, applicationIdentityAttributes);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public ApplicationEntity getApplication(String applicationName)
			throws ApplicationNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		return application;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void updateApplicationCertificate(String applicationName,
			byte[] certificateData) throws CertificateEncodingException,
			ApplicationNotFoundException {
		LOG.debug("updating application certificate for " + applicationName);
		X509Certificate certificate = PkiUtils
				.decodeCertificate(certificateData);

		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);

		application.setCertificate(certificate);
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public void setApplicationDeviceRestriction(String name,
			boolean deviceRestriction) throws ApplicationNotFoundException,
			PermissionDeniedException {
		LOG.debug("set application description: " + name);
		ApplicationEntity application = this.applicationDAO
				.getApplication(name);

		checkWritePermission(application);

		application.setDeviceRestriction(deviceRestriction);
	}
}
