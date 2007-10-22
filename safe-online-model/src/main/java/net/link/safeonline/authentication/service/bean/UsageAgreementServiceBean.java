/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UsageAgreementServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.model.UsageAgreementManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Implementation of usage agreement service interface.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class UsageAgreementServiceBean implements UsageAgreementService,
		UsageAgreementServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(UsageAgreementServiceBean.class);

	@EJB
	private UsageAgreementManager usageAgreementManager;

	@EJB
	private UsageAgreementDAO usageAgreementDAO;

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@Resource
	private SessionContext sessionContext;

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
			throw new PermissionDeniedException(
					"application owner admin mismatch");
		}
	}

	/**
	 * Check write permission on the given application. Only the subject
	 * corresponding with the application owner of the application is allowed to
	 * write to the application entity.
	 * 
	 * @param application
	 * @throws PermissionDeniedException
	 */
	private void checkWritePermission(ApplicationEntity application)
			throws PermissionDeniedException {
		ApplicationOwnerEntity applicationOwner = application
				.getApplicationOwner();
		SubjectEntity requiredSubject = applicationOwner.getAdmin();
		SubjectEntity actualSubject = this.subjectManager.getCallerSubject();
		if (false == requiredSubject.equals(actualSubject)) {
			throw new PermissionDeniedException(
					"application owner admin mismatch");
		}
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public UsageAgreementEntity createDraftUsageAgreement(
			String applicationName, Long usageAgreementVersion)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("create draft usage agreement for application: "
				+ applicationName + " from version: " + usageAgreementVersion);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkWritePermission(application);

		UsageAgreementEntity draftUsageAgreement = this.usageAgreementDAO
				.addUsageAgreement(application,
						UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);

		UsageAgreementEntity usageAgreement = this.usageAgreementDAO
				.getUsageAgreement(application, usageAgreementVersion);
		if (null != usageAgreement) {
			for (UsageAgreementTextEntity usageAgreementText : usageAgreement
					.getUsageAgreementTexts()) {
				this.usageAgreementDAO.addUsageAgreementText(
						draftUsageAgreement, usageAgreementText.getText(),
						usageAgreementText.getLanguage());
			}
		}
		return draftUsageAgreement;
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public UsageAgreementTextEntity createDraftUsageAgreementText(
			String applicationName, String language, String text)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("create draft usage agreement text for application: "
				+ applicationName + " language=" + language);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkWritePermission(application);

		UsageAgreementEntity usageAgreement = getDraftUsageAgreement(applicationName);
		UsageAgreementTextEntity usageAgreementText = usageAgreement
				.getUsageAgreementText(language);
		if (null == usageAgreementText)
			usageAgreementText = this.usageAgreementDAO.addUsageAgreementText(
					usageAgreement, text, language);
		return usageAgreementText;
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public void updateUsageAgreement(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("update application usage agreement: " + applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkWritePermission(application);

		this.usageAgreementManager.updateUsageAgreement(application);
	}

	@RolesAllowed( { SafeOnlineRoles.USER_ROLE, SafeOnlineRoles.OWNER_ROLE })
	public UsageAgreementEntity getCurrentUsageAgreement(String applicationName)
			throws PermissionDeniedException, ApplicationNotFoundException {
		LOG.debug("get current usage agreement for application: "
				+ applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkReadPermission(application);

		return this.usageAgreementDAO.getUsageAgreement(application,
				application.getCurrentApplicationUsageAgreement());
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public List<UsageAgreementEntity> getUsageAgreements(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("get usage agreements for application: " + applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkReadPermission(application);

		return this.usageAgreementDAO.listUsageAgreements(application);
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public UsageAgreementEntity getDraftUsageAgreement(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("get draft usage agreement for application: "
				+ applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkReadPermission(application);

		return this.usageAgreementDAO.getUsageAgreement(application,
				UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public void setDraftUsageAgreementText(String applicationName,
			String language, String text) throws ApplicationNotFoundException,
			PermissionDeniedException {
		LOG.debug("set draft usage agreement text for application: "
				+ applicationName + " language=" + language);

		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkWritePermission(application);

		UsageAgreementEntity usageAgreement = this.usageAgreementDAO
				.getUsageAgreement(application,
						UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);

		UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO
				.getUsageAgreementText(usageAgreement, language);
		usageAgreementText.setText(text);
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public void removeDraftUsageAgreementText(String applicationName,
			String language) throws ApplicationNotFoundException,
			PermissionDeniedException {
		LOG.debug("remove draft usage agreement text for application: "
				+ applicationName + " language=" + language);

		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkWritePermission(application);

		UsageAgreementEntity usageAgreement = this.usageAgreementDAO
				.getUsageAgreement(application,
						UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
		UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO
				.getUsageAgreementText(usageAgreement, language);

		this.usageAgreementDAO.removeUsageAgreementText(usageAgreementText);
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public void removeDraftUsageAgreement(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException {
		LOG.debug("remove draft usage agreement for application: "
				+ applicationName);

		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		checkWritePermission(application);

		this.usageAgreementDAO.removeusageAgreement(application,
				UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean requiresUsageAgreementAcceptation(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("is confirmation required for application " + applicationName
				+ " by subject " + subject.getUserId());

		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long currentUsageAgreementVersion = application
				.getCurrentApplicationUsageAgreement();
		SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(
				subject, application);

		long confirmedUsageAgreementVersion = subscription
				.getConfirmedUsageAgreementVersion();

		LOG
				.debug("application version: "
						+ currentUsageAgreementVersion
						+ " subscription version: "
						+ subscription.getConfirmedUsageAgreementVersion()
						+ " equals?"
						+ (currentUsageAgreementVersion == currentUsageAgreementVersion));

		if (confirmedUsageAgreementVersion != currentUsageAgreementVersion) {
			return true;
		}
		return false;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void confirmUsageAgreementVersion(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("confirm usage agreement for application " + applicationName
				+ " by subject " + subject.getUserId());

		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		Long currentUsageAgreementVersion = application
				.getCurrentApplicationUsageAgreement();
		SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(
				subject, application);
		subscription
				.setConfirmedUsageAgreementVersion(currentUsageAgreementVersion);

	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String getUsageAgreementText(String applicationName, String language)
			throws ApplicationNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		Long usageAgreementVersion = application
				.getCurrentApplicationUsageAgreement();
		UsageAgreementEntity usageAgreement = this.usageAgreementDAO
				.getUsageAgreement(application, usageAgreementVersion);
		if (null == usageAgreement)
			return null;
		UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO
				.getUsageAgreementText(usageAgreement, language);
		if (null == usageAgreementText)
			return null;
		return usageAgreementText.getText();
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String getUsageAgreementText(String applicationName,
			String language, Long usageAgreementVersion)
			throws ApplicationNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		UsageAgreementEntity usageAgreement = this.usageAgreementDAO
				.getUsageAgreement(application, usageAgreementVersion);
		if (null == usageAgreement)
			return null;
		UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO
				.getUsageAgreementText(usageAgreement, language);
		if (null == usageAgreementText)
			return null;
		return usageAgreementText.getText();
	}
}
