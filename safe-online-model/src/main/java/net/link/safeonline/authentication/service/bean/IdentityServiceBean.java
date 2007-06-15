/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
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
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.AttributeTypeDescriptionDecorator;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.util.FilterUtil;
import net.link.safeonline.util.MapEntryFilter;

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

	/**
	 * Gives back the attribute type for the given attribute name, but only if
	 * the user is allowed to edit attributes of the attribute type.
	 * 
	 * @param attributeName
	 * @return
	 * @throws PermissionDeniedException
	 */
	private AttributeTypeEntity getUserEditableAttributeType(
			String attributeName) throws PermissionDeniedException {
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.findAttributeType(attributeName);
		if (null == attributeType) {
			throw new IllegalArgumentException("attribute type not found: "
					+ attributeName);
		}
		if (false == attributeType.isUserEditable()) {
			throw new PermissionDeniedException();
		}
		return attributeType;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void saveAttribute(AttributeDO attribute)
			throws PermissionDeniedException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String attributeName = attribute.getName();
		long index = attribute.getIndex();
		LOG.debug("save attribute " + attributeName + " for entity with login "
				+ subject + "; index " + index);
		LOG.debug("received attribute values: " + attribute);

		AttributeTypeEntity attributeType = getUserEditableAttributeType(attributeName);

		boolean multiValued = attributeType.isMultivalued();
		if (false == multiValued) {
			if (0 != index) {
				throw new IllegalArgumentException(
						"index cannot <> 0 on single-valued attribute type");
			}
		}

		DatatypeType type = attributeType.getType();
		if (attribute.getType() != type) {
			throw new EJBException("datatype does not match");
		}
		switch (type) {
		case STRING: {
			String attributeValue = attribute.getStringValue();
			this.attributeDAO.addOrUpdateAttribute(attributeType, subject,
					index, attributeValue);
			break;
		}
		case BOOLEAN: {
			Boolean attributeValue = attribute.getBooleanValue();
			this.attributeDAO.addOrUpdateAttribute(attributeType, subject,
					index, attributeValue);
			break;
		}
		default:
			throw new EJBException("datatype not supported: " + type);
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
			DatatypeType datatype = attributeType.getType();
			boolean multivalued = attributeType.isMultivalued();
			long index = attribute.getAttributeIndex();

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
					multivalued, index, humanReadableName, description,
					editable, true, stringValue, booleanValue);
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
		Set<ApplicationIdentityAttributeEntity> identityAttributeTypes = applicationIdentity
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
			if (attributeType.isCompounded()) {
				/*
				 * Of course we don't create value entries for compounded
				 * top-level attributes.
				 */
				continue;
			}
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
		Set<ApplicationIdentityAttributeEntity> currentIdentityAttributes = applicationIdentity
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
			LOG
					.debug("currentIdentityAttributes: "
							+ currentIdentityAttributes);
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
		List<AttributeDO> missingAttributes = listMissingAttributes(
				applicationName, null);
		return false == missingAttributes.isEmpty();
	}

	/**
	 * Gives back all the requires data attribute types for the given
	 * application. This method will also expand compounded attribute types.
	 * 
	 * @param applicationName
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 */
	private Set<AttributeTypeEntity> getRequiredDataAttributeTypes(
			String applicationName) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationName);
		long currentApplicationIdentityVersion = application
				.getCurrentApplicationIdentity();
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application,
						currentApplicationIdentityVersion);
		Set<ApplicationIdentityAttributeEntity> identityAttributes = applicationIdentity
				.getAttributes();

		/*
		 * The non-compounded attribute types have precedence over the members
		 * of compounded attribute types.
		 */
		Map<AttributeTypeEntity, Boolean> attributeRequirements = new HashMap<AttributeTypeEntity, Boolean>();
		for (ApplicationIdentityAttributeEntity identityAttribute : identityAttributes) {
			AttributeTypeEntity attributeType = identityAttribute
					.getAttributeType();
			if (attributeType.isCompounded()) {
				continue;
			}
			attributeRequirements.put(attributeType, identityAttribute
					.isRequired());
		}

		/*
		 * Next we go over the compounded attribute types and add their members
		 * to the map, using the optionality of the member attribute entity.
		 */
		for (ApplicationIdentityAttributeEntity identityAttribute : identityAttributes) {
			AttributeTypeEntity attributeType = identityAttribute
					.getAttributeType();
			if (false == attributeType.isCompounded()) {
				continue;
			}
			if (false == identityAttribute.isRequired()) {
				continue;
			}
			for (CompoundedAttributeTypeMemberEntity member : attributeType
					.getMembers()) {
				AttributeTypeEntity memberAttributeType = member.getMember();
				if (attributeRequirements.containsKey(memberAttributeType)) {
					/*
					 * If the attribute is already present it's because of a
					 * non-compounded attribute type which has precedence over
					 * the member attribute types of a compounded attribute
					 * type.
					 */
					continue;
				}
				attributeRequirements.put(memberAttributeType, member
						.isRequired());
			}
		}

		Set<AttributeTypeEntity> result = FilterUtil.filterToSet(
				attributeRequirements, new RequiredAttributeMapEntryFilter());
		return result;
	}

	private static class RequiredAttributeMapEntryFilter implements
			MapEntryFilter<AttributeTypeEntity, Boolean> {

		public boolean isAllowed(Entry<AttributeTypeEntity, Boolean> element) {
			return element.getValue();
		}
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listMissingAttributes(String applicationName,
			Locale locale) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException {
		LOG.debug("get missing attribute for application: " + applicationName);
		Set<AttributeTypeEntity> requiredApplicationAttributeTypes = getRequiredDataAttributeTypes(applicationName);

		SubjectEntity subject = this.subjectManager.getCallerSubject();
		Map<AttributeTypeEntity, List<AttributeEntity>> userAttributes = this.attributeDAO
				.listAttributes(subject);

		for (Map.Entry<AttributeTypeEntity, List<AttributeEntity>> userAttributeEntry : userAttributes
				.entrySet()) {
			/*
			 * Even in case of a multi-valued attribute we only need to peek at
			 * the first entry.
			 */
			AttributeEntity userAttribute = userAttributeEntry.getValue()
					.get(0);
			DatatypeType datatype = userAttribute.getAttributeType().getType();
			switch (datatype) {
			case STRING:
				String stringValue = userAttribute.getStringValue();
				if (null == stringValue) {
					/*
					 * In this case the user still needs to input a value for
					 * the field.
					 */
					continue;
				}
				if (stringValue.length() == 0) {
					/*
					 * Even empty attributes must be marked as missing.
					 */
					continue;
				}
				break;
			case BOOLEAN:
				Boolean booleanValue = userAttribute.getBooleanValue();
				if (null == booleanValue) {
					/*
					 * In this case the user still needs to input a value for
					 * the boolean attribute.
					 */
					continue;
				}
				break;
			default:
				throw new EJBException("datatype not supported: " + datatype);
			}
			requiredApplicationAttributeTypes.remove(userAttribute
					.getAttributeType());
		}

		/*
		 * At this point the set contains the required attribute types for which
		 * the user has not yet entered a value. Some of these attribute types
		 * are belong to a compounded attribute type. If this is the case we
		 * have to return the entire compounded attribute record to the user so
		 * he can edit the missing fields of the compounded attribute 'in the
		 * correct context'.
		 */

		/*
		 * Construct the result view. On top of the view list we put the
		 * attribute entries that are not a member of a compounded attribute
		 * type.
		 */
		List<AttributeDO> missingAttributes = new LinkedList<AttributeDO>();
		for (AttributeTypeEntity attributeType : requiredApplicationAttributeTypes) {
			if (attributeType.isCompoundMember()) {
				continue;
			}
			String humanReadableName = null;
			String description = null;
			DatatypeType datatype = attributeType.getType();
			String attributeName = attributeType.getName();
			if (null != locale) {
				String language = locale.getLanguage();
				LOG.debug("trying language: " + language);
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(
								attributeName, language));
				if (null != attributeTypeDescription) {
					LOG.debug("found description");
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			/*
			 * We mark the missing attribute as singled-valued here since
			 * basically the user does not care at this point.
			 */
			AttributeDO missingAttribute = new AttributeDO(attributeName,
					datatype, false, 0, humanReadableName, description, true,
					true, null, null);
			LOG.debug("adding missing attribute: " + attributeName);
			missingAttributes.add(missingAttribute);
		}

		/*
		 * Next we add the compounded attribute records for the missing member
		 * attribute types. First we need to construct the list of compounded
		 * attribute types. Notice here that we first need to construct the set
		 * to filter out duplicate entries.
		 */
		Set<AttributeTypeEntity> compoundedAttributeTypes = new HashSet<AttributeTypeEntity>();
		for (AttributeTypeEntity attributeType : requiredApplicationAttributeTypes) {
			if (false == attributeType.isCompoundMember()) {
				continue;
			}
			AttributeTypeEntity parentAttributeType;
			try {
				parentAttributeType = this.attributeTypeDAO
						.getParent(attributeType);
			} catch (AttributeTypeNotFoundException e) {
				throw new EJBException(
						"inconsistency in compounded attribute type definition of "
								+ attributeType.getName());
			}
			compoundedAttributeTypes.add(parentAttributeType);
		}

		/*
		 * Finally we add a record for every required missing compounded
		 * attribute type.
		 */
		for (AttributeTypeEntity compoundedAttributeType : compoundedAttributeTypes) {
			/*
			 * First we add the 'top-level' compounded title entry.
			 */
			String humanReadableName = null;
			String description = null;
			if (null != locale) {
				String language = locale.getLanguage();
				LOG.debug("trying language: " + language);
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(
								compoundedAttributeType.getName(), language));
				if (null != attributeTypeDescription) {
					LOG.debug("found description");
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			AttributeDO missingAttribute = new AttributeDO(
					compoundedAttributeType.getName(), DatatypeType.COMPOUNDED,
					false, 0, humanReadableName, description, true, true, null,
					null);
			missingAttribute.setCompounded(true);
			LOG.debug("adding missing compounded attribute: "
					+ missingAttribute.getName());
			missingAttributes.add(missingAttribute);
			/*
			 * Under the title entry we add entries for the members of the
			 * compounded attribute type.
			 */
			for (CompoundedAttributeTypeMemberEntity member : compoundedAttributeType
					.getMembers()) {
				AttributeTypeEntity attributeType = member.getMember();
				if (null != locale) {
					String language = locale.getLanguage();
					LOG.debug("trying language: " + language);
					AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
							.findDescription(new AttributeTypeDescriptionPK(
									attributeType.getName(), language));
					if (null != attributeTypeDescription) {
						LOG.debug("found description");
						humanReadableName = attributeTypeDescription.getName();
						description = attributeTypeDescription.getDescription();
					}
				} else {
					humanReadableName = null;
					description = null;
				}
				AttributeDO missingMemberAttribute = new AttributeDO(
						attributeType.getName(), attributeType.getType(),
						false, 0, humanReadableName, description, true, true,
						null, null);
				missingMemberAttribute.setMember(true);

				/*
				 * For a compounded we want to fill in as much existing data as
				 * possible to make it easier for the user to edit the values in
				 * context of an existing compounded attribute.
				 */
				List<AttributeEntity> attributes = userAttributes
						.get(attributeType);
				if (null != attributes) {
					AttributeEntity attribute = attributes.get(0);
					DatatypeType type = attributeType.getType();
					switch (type) {
					case STRING:
						missingMemberAttribute.setStringValue(attribute
								.getStringValue());
						break;
					case BOOLEAN:
						missingMemberAttribute.setBooleanValue(attribute
								.getBooleanValue());
						break;
					default:
						throw new EJBException("unsupported data type: " + type);
					}
				}
				LOG.debug("adding missing member attribute: "
						+ missingMemberAttribute.getName());
				missingAttributes.add(missingMemberAttribute);
			}
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

	@EJB
	private DeviceDAO deviceDAO;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listAttributes(String deviceId, Locale locale)
			throws DeviceNotFoundException {
		LOG.debug("list attributes for device: " + deviceId);
		DeviceEntity device = this.deviceDAO.getDevice(deviceId);
		List<AttributeTypeEntity> deviceAttributeTypes = device
				.getAttributeTypes();
		List<AttributeDO> attributes = new LinkedList<AttributeDO>();

		SubjectEntity subject = this.subjectManager.getCallerSubject();

		String language;
		if (null == locale) {
			language = null;
		} else {
			language = locale.getLanguage();
		}

		LOG.debug("# device attributes: " + deviceAttributeTypes.size());
		for (AttributeTypeEntity attributeType : deviceAttributeTypes) {
			LOG.debug("attribute type: " + attributeType.getName());
			if (false == attributeType.isUserVisible()) {
				continue;
			}

			boolean multivalued = attributeType.isMultivalued();
			String name = attributeType.getName();
			DatatypeType type = attributeType.getType();
			boolean editable = attributeType.isUserEditable();
			boolean dataMining = false;
			String humanReabableName = null;
			String description = null;
			if (null != language) {
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(name,
								language));
				if (null != attributeTypeDescription) {
					humanReabableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			AttributeEntity attribute = this.attributeDAO.findAttribute(
					attributeType, subject);
			String stringValue;
			Boolean booleanValue;
			long index;
			if (null != attribute) {
				stringValue = attribute.getStringValue();
				booleanValue = attribute.getBooleanValue();
				index = attribute.getAttributeIndex();
			} else {
				stringValue = null;
				booleanValue = null;
				index = 0;
			}
			AttributeDO attributeView = new AttributeDO(name, type,
					multivalued, index, humanReabableName, description,
					editable, dataMining, stringValue, booleanValue);
			attributes.add(attributeView);
		}
		return attributes;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void removeAttribute(AttributeDO attribute)
			throws PermissionDeniedException, AttributeNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String attributeName = attribute.getName();
		LOG.debug("remove attribute " + attributeName
				+ " for entity with login " + subject);
		LOG.debug("received attribute values: " + attribute);

		AttributeTypeEntity attributeType = getUserEditableAttributeType(attributeName);

		boolean multivalued = attributeType.isMultivalued();
		if (false == multivalued) {
			AttributeEntity attributeEntity = this.attributeDAO.getAttribute(
					attributeType, subject);
			this.attributeDAO.removeAttribute(attributeEntity);
		} else {
			/*
			 * In case the attribute to be removed is part of a multivalued
			 * attribute we have to resequence the remaining attributes.
			 */
			List<AttributeEntity> attributes = this.attributeDAO
					.listAttributes(subject, attributeType);
			if (attributes.isEmpty()) {
				throw new AttributeNotFoundException();
			}
			long index = attribute.getIndex();
			Iterator<AttributeEntity> iterator = attributes.iterator();
			AttributeEntity removeAttribute = null;
			while (iterator.hasNext()) {
				AttributeEntity iterAttribute = iterator.next();
				if (index == iterAttribute.getAttributeIndex()) {
					removeAttribute = iterAttribute;
					break;
				}
			}
			if (null == removeAttribute) {
				throw new AttributeNotFoundException();
			}
			/*
			 * We remove by moving the data of the following remaining
			 * attributes one up, and finally we remove the last entry in the
			 * list.
			 */
			while (iterator.hasNext()) {
				AttributeEntity nextAttribute = iterator.next();
				/*
				 * By copying the content of the next attribute into the remove
				 * attribute we basically reindex the attributes. We cannot just
				 * change the attribute index since it is part of the compounded
				 * primary key of the attribute entity. Maybe we should use a
				 * global PK attribute Id and a separate viewId instead?
				 */
				removeAttribute
						.setBooleanValue(nextAttribute.getBooleanValue());
				removeAttribute.setStringValue(nextAttribute.getStringValue());
				removeAttribute = nextAttribute;
			}
			this.attributeDAO.removeAttribute(removeAttribute);
		}
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void addAttribute(AttributeDO newAttribute)
			throws PermissionDeniedException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String attributeName = newAttribute.getName();
		LOG.debug("add attribute " + attributeName + " for entity with login "
				+ subject);

		AttributeTypeEntity attributeType = getUserEditableAttributeType(attributeName);

		boolean multivalued = attributeType.isMultivalued();
		if (false == multivalued) {
			throw new PermissionDeniedException();
		}

		AttributeEntity attribute = this.attributeDAO.addAttribute(
				attributeType, subject);

		LOG.debug("new attribute index: " + attribute.getAttributeIndex());

		/*
		 * Also copy the data into the new persisted attribute.
		 */
		DatatypeType datatype = attributeType.getType();
		switch (datatype) {
		case STRING:
			attribute.setStringValue(newAttribute.getStringValue());
			break;
		case BOOLEAN:
			attribute.setBooleanValue(newAttribute.getBooleanValue());
			break;
		default:
			throw new EJBException("datatype not supported: " + datatype);
		}
	}
}
