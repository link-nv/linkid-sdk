/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

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
import net.link.safeonline.authentication.service.IdentityServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.AttributeTypeDescriptionDecorator;
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
public class IdentityServiceBean implements IdentityService,
		IdentityServiceRemote {

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

	@EJB
	AttributeTypeDescriptionDecorator attributeTypeDescriptionDecorator;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<HistoryEntity> listHistory() {
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
	public void saveAttribute(AttributeDO attribute)
			throws PermissionDeniedException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String attributeName = attribute.getName();
		LOG.debug("save attribute " + attributeName + " for entity with login "
				+ subject);

		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.findAttributeType(attributeName);
		if (null == attributeType) {
			throw new IllegalArgumentException("attribute type not found: "
					+ attributeName);
		}
		if (false == attributeType.isUserEditable()) {
			throw new PermissionDeniedException();
		}

		String type = attributeType.getType();
		if (SafeOnlineConstants.STRING_TYPE.equals(type)) {
			String attributeValue = attribute.getStringValue();
			this.attributeDAO.addOrUpdateAttribute(attributeType, subject,
					attributeValue);
		} else if (SafeOnlineConstants.BOOLEAN_TYPE.equals(type)) {
			Boolean attributeValue = attribute.getBooleanValue();
			this.attributeDAO.addOrUpdateAttribute(attributeType, subject,
					attributeValue);
		}
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listAttributes(Locale locale) {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("get attributes for " + subject.getLogin());
		List<AttributeEntity> attributes = this.attributeDAO
				.listVisibleAttributes(subject);
		LOG.debug("number of attributes: " + attributes.size());
		List<AttributeDO> attributesView = new LinkedList<AttributeDO>();
		for (AttributeEntity attribute : attributes) {
			LOG.debug("attribute pk type: "
					+ attribute.getPk().getAttributeType());
			AttributeTypeEntity attributeType = attribute.getAttributeType();
			LOG.debug("attribute type: " + attributeType.getName());
			String name = attributeType.getName();
			String stringValue = attribute.getStringValue();
			Boolean booleanValue = attribute.getBooleanValue();
			boolean editable = attributeType.isUserEditable();
			String datatype = attributeType.getType();

			String humanReadableName = null;
			String description = null;
			if (null != locale) {
				String language = locale.getLanguage();
				LOG.debug("trying language: " + language);
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(name,
								language));
				if (null != attributeTypeDescription) {
					LOG.debug("found description");
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}

			AttributeDO attributeView = new AttributeDO(name, datatype,
					humanReadableName, description, editable, true,
					stringValue, booleanValue);
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
		List<ApplicationIdentityAttributeEntity> identityAttributeTypes = applicationIdentity
				.getAttributes();
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
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException {
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

		manageIdentityAttributeVisibility(application, subject, subscription);
	}

	/**
	 * Manages the visibility of the identity attributes towards the subject.
	 * This is done by creating the non-existing attribute entities for the
	 * confirmed identity.
	 * 
	 * @param application
	 * @param subject
	 * @param subscription
	 * @throws ApplicationIdentityNotFoundException
	 */
	private void manageIdentityAttributeVisibility(
			ApplicationEntity application, SubjectEntity subject,
			SubscriptionEntity subscription)
			throws ApplicationIdentityNotFoundException {
		ApplicationIdentityEntity confirmedApplicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, subscription
						.getConfirmedIdentityVersion());
		List<AttributeTypeEntity> attributeTypes = confirmedApplicationIdentity
				.getAttributeTypes();
		for (AttributeTypeEntity attributeType : attributeTypes) {
			AttributeEntity existingAttribute = this.attributeDAO
					.findAttribute(attributeType, subject);
			if (null != existingAttribute) {
				continue;
			}
			this.attributeDAO.addAttribute(attributeType, subject, null);
		}
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listIdentityAttributesToConfirm(
			String applicationName, Locale locale)
			throws ApplicationNotFoundException,
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
		List<ApplicationIdentityAttributeEntity> currentIdentityAttributes = applicationIdentity
				.getAttributes();

		SubjectEntity subject = this.subjectManager.getCallerSubject();
		SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(
				subject, application);
		Long confirmedIdentityVersion = subscription
				.getConfirmedIdentityVersion();

		if (null == confirmedIdentityVersion) {
			/*
			 * If no identity version was confirmed previously, then the user
			 * needs to confirm the current application identity attributes.
			 */

			List<AttributeDO> resultAttributes = this.attributeTypeDescriptionDecorator
					.addDescriptionFromIdentityAttributes(
							currentIdentityAttributes, locale);
			return resultAttributes;
		}

		ApplicationIdentityEntity confirmedApplicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, confirmedIdentityVersion);
		List<AttributeTypeEntity> confirmedAttributeTypes = confirmedApplicationIdentity
				.getAttributeTypes();

		List<AttributeTypeEntity> toConfirmAttributes = new LinkedList<AttributeTypeEntity>();
		toConfirmAttributes.addAll(applicationIdentity.getAttributeTypes());
		/*
		 * Be careful here not to edit the currentIdentityAttributeTypes list
		 * itself.
		 */
		toConfirmAttributes.removeAll(confirmedAttributeTypes);
		List<AttributeDO> resultAttributes = this.attributeTypeDescriptionDecorator
				.addDescriptionFromAttributeTypes(toConfirmAttributes, locale);
		return resultAttributes;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean hasMissingAttributes(String applicationName)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException {
		LOG.debug("hasMissingAttributes for application: " + applicationName);
		List<AttributeDO> missingAttributes = getMissingAttributes(
				applicationName, null);
		return false == missingAttributes.isEmpty();
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> getMissingAttributes(String applicationName,
			Locale locale) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException {
		// TODO: simplify this method implementation
		LOG.debug("get missing attribute for application: " + applicationName);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long currentApplicationIdentityVersion = application
				.getCurrentApplicationIdentity();
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application,
						currentApplicationIdentityVersion);
		List<AttributeTypeEntity> requiredApplicationAttributeTypes = applicationIdentity
				.getRequiredAttributeTypes();

		SubjectEntity subject = this.subjectManager.getCallerSubject();
		List<AttributeEntity> userAttributes = this.attributeDAO
				.listAttributes(subject);

		Set<String> missingAttributeNames = new TreeSet<String>();
		for (AttributeTypeEntity applicationAttributeType : requiredApplicationAttributeTypes) {
			missingAttributeNames.add(applicationAttributeType.getName());
		}

		for (AttributeEntity userAttribute : userAttributes) {
			if (null == userAttribute.getStringValue()) {
				/*
				 * In this case the user still needs to input a value for the
				 * field.
				 */
				continue;
			}
			if (userAttribute.getStringValue().length() == 0) {
				/*
				 * Even empty attributes must be marked as missing.
				 */
				continue;
			}
			String attributeName = userAttribute.getAttributeType().getName();
			missingAttributeNames.remove(attributeName);
		}

		/*
		 * Construct the result view.
		 */
		List<AttributeDO> missingAttributes = new LinkedList<AttributeDO>();
		for (String missingAttributeName : missingAttributeNames) {
			String humanReadableName = null;
			String description = null;
			AttributeTypeEntity attributeType = this.attributeTypeDAO
					.findAttributeType(missingAttributeName);
			String datatype = attributeType.getType();
			if (null != locale) {
				String language = locale.getLanguage();
				LOG.debug("trying language: " + language);
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(
								missingAttributeName, language));
				if (null != attributeTypeDescription) {
					LOG.debug("found description");
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			AttributeDO missingAttribute = new AttributeDO(
					missingAttributeName, datatype, humanReadableName,
					description, true, true, null, null);
			missingAttributes.add(missingAttribute);
		}

		return missingAttributes;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listConfirmedIdentity(String applicationName,
			Locale locale) throws ApplicationNotFoundException,
			SubscriptionNotFoundException, ApplicationIdentityNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(
				subject, application);
		Long confirmedIdentityVersion = subscription
				.getConfirmedIdentityVersion();
		if (null == confirmedIdentityVersion) {
			return new LinkedList<AttributeDO>();
		}
		ApplicationIdentityEntity confirmedIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, confirmedIdentityVersion);
		List<AttributeTypeEntity> confirmedAttributeTypes = confirmedIdentity
				.getAttributeTypes();
		List<AttributeDO> confirmedAttributes = this.attributeTypeDescriptionDecorator
				.addDescriptionFromAttributeTypes(confirmedAttributeTypes,
						locale);
		return confirmedAttributes;
	}
}
