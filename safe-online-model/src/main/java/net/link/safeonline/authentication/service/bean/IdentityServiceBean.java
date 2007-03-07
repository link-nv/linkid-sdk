/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Implementation of identity service.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class IdentityServiceBean implements IdentityService {

	private static final Log LOG = LogFactory.getLog(IdentityServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private ApplicationIdentityDAO applicationIdentityDAO;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<HistoryEntity> getHistory() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		List<HistoryEntity> result = this.historyDAO.getHistory(subject);
		return result;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String findAttributeValue(String attributeName)
			throws PermissionDeniedException {
		String subjectLogin = this.subjectManager.getCallerLogin();
		LOG.debug("get attribute " + attributeName + " for user with login "
				+ subjectLogin);
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, subjectLogin);
		if (null == attribute) {
			return null;
		}
		if (false == attribute.getAttributeType().isUserVisible()) {
			throw new PermissionDeniedException();
		}
		String value = attribute.getStringValue();
		return value;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void saveAttribute(String attributeName, String attributeValue)
			throws PermissionDeniedException {
		String login = this.subjectManager.getCallerLogin();
		LOG.debug("save attribute " + attributeName + " for entity with login "
				+ login);

		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.findAttributeType(attributeName);
		if (null == attributeType) {
			throw new IllegalArgumentException("attribute type not found: "
					+ attributeName);
		}
		if (false == attributeType.isUserEditable()) {
			throw new PermissionDeniedException();
		}

		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, login);
		if (null == attribute) {
			this.attributeDAO
					.addAttribute(attributeType, login, attributeValue);
			return;
		}
		attribute.setStringValue(attributeValue);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> getAttributes() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("get attributes for " + subject.getLogin());
		List<AttributeEntity> attributes = this.attributeDAO
				.getAttributes(subject);
		LOG.debug("number of attributes: " + attributes.size());
		List<AttributeDO> attributesView = new LinkedList<AttributeDO>();
		for (AttributeEntity attribute : attributes) {
			LOG.debug("attribute pk type: "
					+ attribute.getPk().getAttributeType());
			AttributeTypeEntity attributeType = attribute.getAttributeType();
			LOG.debug("attribute type: " + attributeType.getName());
			if (false == attributeType.isUserVisible()) {
				LOG.debug("attribute not user visible");
				continue;
			}
			String name = attributeType.getName();
			String value = attribute.getStringValue();
			boolean editable = attributeType.isUserEditable();
			AttributeDO attributeView = new AttributeDO(name, value, editable);
			attributesView.add(attributeView);
		}
		return attributesView;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean isConfirmationRequired(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("is confirmation required for application " + applicationName
				+ " by subject " + subject.getLogin());

		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long currentIdentityVersion = application
				.getCurrentApplicationIdentity();
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, currentIdentityVersion);
		List<AttributeTypeEntity> identityAttributeTypes = applicationIdentity
				.getAttributeTypes();
		if (true == identityAttributeTypes.isEmpty()) {
			/*
			 * If the identity is empty, the user does not need to do the
			 * explicit confirmation.
			 */
			return false;
		}

		SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(
				subject, application);
		if (null == subscription.getConfirmedIdentityVersion()) {
			/*
			 * In this case the user did not yet confirm any identity version
			 * yet.
			 */
			return true;
		}

		long confirmedIdentityVersion = subscription
				.getConfirmedIdentityVersion();

		if (currentIdentityVersion != confirmedIdentityVersion) {
			return true;
		}
		return false;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void confirmIdentity(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException {
		LOG.debug("confirm identity for application: " + applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long currentApplicationIdentityVersion = application
				.getCurrentApplicationIdentity();

		SubjectEntity subject = this.subjectManager.getCallerSubject();
		SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(
				subject, application);

		subscription
				.setConfirmedIdentityVersion(currentApplicationIdentityVersion);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeTypeEntity> getIdentityAttributesToConfirm(
			String applicationName) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException, SubscriptionNotFoundException {
		LOG
				.debug("get identity to confirm for application: "
						+ applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long currentApplicationIdentityVersion = application
				.getCurrentApplicationIdentity();
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application,
						currentApplicationIdentityVersion);
		List<AttributeTypeEntity> currentIdentityAttributeTypes = applicationIdentity
				.getAttributeTypes();

		SubjectEntity subject = this.subjectManager.getCallerSubject();
		SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(
				subject, application);
		Long confirmedIdentityVersion = subscription
				.getConfirmedIdentityVersion();

		if (null == confirmedIdentityVersion) {
			/*
			 * If no identity version was confirmed previously, then the user
			 * needs to confirm the current application identity.
			 */
			return currentIdentityAttributeTypes;
		}

		ApplicationIdentityEntity confirmedApplicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, confirmedIdentityVersion);
		List<AttributeTypeEntity> confirmedAttributeTypes = confirmedApplicationIdentity
				.getAttributeTypes();

		List<AttributeTypeEntity> toConfirmAttributes = new LinkedList<AttributeTypeEntity>();
		toConfirmAttributes.addAll(currentIdentityAttributeTypes);
		/*
		 * Be careful here not to edit the currentIdentityAttributeTypes list
		 * itself.
		 */
		toConfirmAttributes.removeAll(confirmedAttributeTypes);
		return toConfirmAttributes;
	}
}
