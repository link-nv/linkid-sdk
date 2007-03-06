/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
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
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.ApplicationOwnerManager;
import net.link.safeonline.model.PkiUtils;
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
public class ApplicationServiceBean implements ApplicationService {

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

	@PermitAll
	public List<ApplicationEntity> getApplications() {
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications();
		return applications;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addApplication(String name, String applicationOwnerName,
			String description, byte[] encodedCertificate,
			String[] initialApplicationIdentityAttributeTypes)
			throws ExistingApplicationException,
			ApplicationOwnerNotFoundException, CertificateEncodingException,
			AttributeTypeNotFoundException {
		LOG.debug("add application: " + name);
		ApplicationEntity existingApplication = this.applicationDAO
				.findApplication(name);
		if (null != existingApplication) {
			throw new ExistingApplicationException();
		}

		X509Certificate certificate = PkiUtils
				.decodeCertificate(encodedCertificate);

		ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
				.getApplicationOwner(applicationOwnerName);

		List<AttributeTypeEntity> identityAttributeTypes = new LinkedList<AttributeTypeEntity>();
		for (String initialApplicationIdentityAttributeType : initialApplicationIdentityAttributeTypes) {
			AttributeTypeEntity attributeType = this.attributeTypeDAO
					.getAttributeType(initialApplicationIdentityAttributeType);
			identityAttributeTypes.add(attributeType);
		}

		ApplicationEntity application = this.applicationDAO.addApplication(
				name, applicationOwner, description, certificate);

		long initialIdentityVersion = ApplicationIdentityPK.INITIAL_IDENTITY_VERSION;
		this.applicationIdentityDAO.addApplicationIdentity(application,
				initialIdentityVersion, identityAttributeTypes);
		application.setCurrentApplicationIdentity(initialIdentityVersion);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeApplication(String name)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("remove application: " + name);
		ApplicationEntity application = this.applicationDAO
				.getApplication(name);
		if (!application.isRemovable()) {
			throw new PermissionDeniedException();
		}
		List<SubscriptionEntity> subscriptions = this.subscriptionDAO
				.getSubscriptions(application);
		/*
		 * We don't rely on hibernate here to cascade remove the subscriptions
		 * and application identities for the moment. Postpone this until be
		 * understand better what data needs to be preserved.
		 */
		for (SubscriptionEntity subscription : subscriptions) {
			this.subscriptionDAO.removeSubscription(subscription);
		}

		List<ApplicationIdentityEntity> applicationIdentities = this.applicationIdentityDAO
				.getApplicationIdentities(application);
		for (ApplicationIdentityEntity applicationIdentity : applicationIdentities) {
			this.applicationIdentityDAO
					.removeApplicationIdentity(applicationIdentity);
		}

		this.applicationDAO.removeApplication(application);
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public void setApplicationDescription(String name, String description)
			throws ApplicationNotFoundException {
		LOG.debug("set application description: " + name);
		ApplicationEntity application = this.applicationDAO
				.getApplication(name);
		application.setDescription(description);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void registerApplicationOwner(String name, String login)
			throws SubjectNotFoundException, ApplicationNotFoundException,
			ExistingApplicationOwnerException {
		LOG.debug("register application owner: " + name + " with account "
				+ login);
		ApplicationOwnerEntity existingApplicationOwner = this.applicationOwnerDAO
				.findApplicationOwner(name);
		if (null != existingApplicationOwner) {
			throw new ExistingApplicationOwnerException();
		}

		SubjectEntity subject = this.subjectDAO.getSubject(login);

		this.applicationOwnerDAO.addApplicationOwner(name, subject);

		ApplicationEntity ownerApplication = this.applicationDAO
				.getApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);

		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.APPLICATION,
				subject, ownerApplication);

		/*
		 * We have to flush the credential cache for the login here. Else it's
		 * possible that the login cannot logon because JAAS is caching the old
		 * roles that did not include the 'owner' role yet.
		 */
		SecurityManagerUtils.flushCredentialCache(login,
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<ApplicationOwnerEntity> getApplicationOwners() {
		LOG.debug("get application owners");
		List<ApplicationOwnerEntity> applicationOwners = this.applicationOwnerDAO
				.getApplicationOwners();
		return applicationOwners;
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public List<ApplicationEntity> getOwnedApplications() {
		LOG.debug("get owned applications");
		ApplicationOwnerEntity applicationOwner = this.applicationOwnerManager
				.getCallerApplicationOwner();
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications(applicationOwner);
		return applications;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AttributeTypeEntity> getCurrentApplicationIdentity(
			String applicationName) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long currentIdentityVersion = application
				.getCurrentApplicationIdentity();
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, currentIdentityVersion);
		List<AttributeTypeEntity> attributeTypes = applicationIdentity
				.getAttributeTypes();
		return attributeTypes;
	}
}
