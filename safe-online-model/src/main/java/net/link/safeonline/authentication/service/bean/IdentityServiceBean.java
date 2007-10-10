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
import java.util.UUID;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
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
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.AttributeTypeDescriptionDecorator;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.util.FilterUtil;
import net.link.safeonline.util.MapEntryFilter;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

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
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class,
		InputValidation.class })
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

	private AttributeManagerLWBean attributeManager;

	@PostConstruct
	public void postConstructCallback() {
		/*
		 * By injecting the attribute DAO of this session bean in the attribute
		 * manager we are sure that the attribute manager (a lightweight bean)
		 * will live within the same transaction and security context as this
		 * identity service EJB3 session bean.
		 */
		LOG.debug("postConstruct");
		this.attributeManager = new AttributeManagerLWBean(this.attributeDAO);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<HistoryEntity> listHistory() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		List<HistoryEntity> result = this.historyDAO.getHistory(subject);
		return result;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String findAttributeValue(@NonEmptyString
	String attributeName) throws PermissionDeniedException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("get attribute " + attributeName + " for user with login "
				+ subject.getUserId());
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, subject.getUserId());
		if (null == attribute) {
			return null;
		}
		if (false == attribute.getAttributeType().isUserVisible()) {
			throw new PermissionDeniedException("attribute not user visible");
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
	 * @throws AttributeTypeNotFoundException
	 */
	private AttributeTypeEntity getUserEditableAttributeType(@NonEmptyString
	String attributeName) throws PermissionDeniedException,
			AttributeTypeNotFoundException {
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeName);
		if (false == attributeType.isUserEditable()) {
			LOG.debug("user not allowed to edit attribute of type: "
					+ attributeName);
			throw new PermissionDeniedException(
					"user not allowed to edit attribute of type: "
							+ attributeName);
		}
		return attributeType;
	}

	/**
	 * Gives back the attribute type for the given attribute name, but only if
	 * the user is allowed to remove attributes of the attribute type.
	 * 
	 * @param attributeName
	 * @return
	 * @throws PermissionDeniedException
	 * @throws AttributeTypeNotFoundException
	 */
	private AttributeTypeEntity getUserRemovableAttributeType(@NonEmptyString
	String attributeName) throws PermissionDeniedException,
			AttributeTypeNotFoundException {
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.findAttributeType(attributeName);
		if (null == attributeType) {
			throw new IllegalArgumentException("attribute type not found: "
					+ attributeName);
		}
		if (true == attributeType.isUserEditable()) {
			return attributeType;
		}
		if (false == attributeType.isCompoundMember()) {
			String msg = "attribute type is not a compounded member: "
					+ attributeType.getName();
			LOG.debug(msg);
			throw new PermissionDeniedException(msg);
		}
		/*
		 * We make an exception here for compounded member attributes here. Even
		 * if the member attribute type is marked as being non-user-editable the
		 * user is allowed to remove the entry if the compounded attribute type
		 * is editable.
		 */
		AttributeTypeEntity compoundedAttributeType = this.attributeTypeDAO
				.getParent(attributeType);
		if (true == compoundedAttributeType.isUserEditable()) {
			return attributeType;
		}
		String msg = "compounded parent attribute type is not user editable: "
				+ compoundedAttributeType.getName();
		LOG.debug(msg);
		throw new PermissionDeniedException(msg);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void saveAttribute(@NotNull
	AttributeDO attribute) throws PermissionDeniedException,
			AttributeTypeNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String attributeName = attribute.getName();
		long index = attribute.getIndex();
		LOG.debug("save attribute " + attributeName + " for entity with login "
				+ subject + "; index " + index);
		LOG.debug("received attribute value: " + attribute);

		if (attribute.isCompounded()) {
			LOG.debug("save compounded attribute");
			/*
			 * A compounded attribute record has a top-level attribute entry
			 * containing a UUID to uniquely identify the compounded attribute
			 * record.
			 */
			AttributeTypeEntity compoundedAttributeType = getUserEditableAttributeType(attributeName);
			AttributeEntity compoundedAttribute = this.attributeDAO
					.findAttribute(subject, compoundedAttributeType, index);
			if (null == compoundedAttribute) {
				/*
				 * This situation is possible when filling in a compounded
				 * attribute record during the missing attributes phase of the
				 * authentication process.
				 */
				compoundedAttribute = this.attributeDAO.addAttribute(
						compoundedAttributeType, subject, index);
				String compoundedAttributeId = UUID.randomUUID().toString();
				LOG.debug("adding compounded attribute for "
						+ subject.getUserId() + " of type " + attributeName
						+ " with ID " + compoundedAttributeId);
				compoundedAttribute.setStringValue(compoundedAttributeId);
			}
			/*
			 * Notice that, if there is already a compounded attribute for the
			 * given record index, then we don't overwrite it with a new ID. The
			 * idea behind the ID is that it remains constant during the
			 * lifecycle of the compounded attribute record.
			 */
			return;
		}

		if (false == attribute.isEditable()) {
			/*
			 * We allow the web application to pass in saveAttribute calls with
			 * attributes marked as non-editable, that way we have a transparent
			 * handling of attributes in the GUI.
			 */
			LOG.debug("attribute marked as non-editable; skipping");
			return;
		}

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

		AttributeEntity attributeEntity = this.attributeDAO.findAttribute(
				subject, attributeType, index);
		if (null == attributeEntity) {
			attributeEntity = this.attributeDAO.addAttribute(attributeType,
					subject, index);
		}
		attribute.copyValueTo(attributeType, attributeEntity);

		this.historyDAO.addHistoryEntry(subject,
				HistoryEventType.ATTRIBUTE_CHANGE, attributeName, null);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listAttributes(Locale locale)
			throws AttributeTypeNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("get attributes for " + subject.getUserId());
		List<AttributeEntity> attributes = this.attributeDAO
				.listVisibleAttributes(subject);

		String language;
		if (null != locale) {
			language = locale.getLanguage();
		} else {
			language = null;
		}

		/*
		 * At the top of the result list we put the attributes that are not a
		 * member of a compounded attribute.
		 */
		List<AttributeDO> attributesView = new LinkedList<AttributeDO>();
		for (AttributeEntity attribute : attributes) {
			AttributeTypeEntity attributeType = attribute.getAttributeType();

			if (attributeType.isCompoundMember()) {
				continue;
			}
			if (attributeType.isCompounded()) {
				/*
				 * The database also contains attribute entities for the
				 * compounded attribute type itself. This attribute stores the
				 * UUID of the corresponding compounded attribute record. This
				 * UUID is used for identification of the compounded attribute
				 * record by the data web service. This UUID serves no purpose
				 * in the communication between SafeOnline core and the
				 * SafeOnline User Web Application. So we can simply skip this
				 * compounded attribute entry.
				 */
				continue;
			}

			LOG.debug("attribute pk type: "
					+ attribute.getPk().getAttributeType());
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
			if (null != language) {
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
			attributeView.setValue(attribute);
			attributesView.add(attributeView);
		}

		/*
		 * Next we put the compounded member attribute in their compounded
		 * attribute context. First we need to determine which compounded
		 * attribute groups we need to visualize.
		 */

		/*
		 * compoundedAttributes: First map is keyed per compounded attribute
		 * type. Second map is keyed via the compounded member attribute type.
		 * Third map is keyed via attribute index.
		 */
		Map<AttributeTypeEntity, Map<AttributeTypeEntity, Map<Long, AttributeEntity>>> compoundedAttributes = new HashMap<AttributeTypeEntity, Map<AttributeTypeEntity, Map<Long, AttributeEntity>>>();
		/*
		 * numberOfRecordsPerCompounded: per compounded attribute type we keep
		 * track of the number of data records that we're required to visualize.
		 */
		Map<AttributeTypeEntity, Long> numberOfRecordsPerCompounded = new HashMap<AttributeTypeEntity, Long>();
		for (AttributeEntity attribute : attributes) {
			AttributeTypeEntity attributeType = attribute.getAttributeType();
			if (false == attributeType.isCompoundMember()) {
				continue;
			}
			AttributeTypeEntity compoundedAttributeType = this.attributeTypeDAO
					.getParent(attributeType);
			Map<AttributeTypeEntity, Map<Long, AttributeEntity>> members = compoundedAttributes
					.get(compoundedAttributeType);
			if (null == members) {
				members = new HashMap<AttributeTypeEntity, Map<Long, AttributeEntity>>();
				compoundedAttributes.put(compoundedAttributeType, members);
			}
			Map<Long, AttributeEntity> memberAttributes = members
					.get(attributeType);
			if (null == memberAttributes) {
				memberAttributes = new HashMap<Long, AttributeEntity>();
				members.put(attributeType, memberAttributes);
			}
			long attributeIndex = attribute.getAttributeIndex();
			memberAttributes.put(attributeIndex, attribute);

			Long numberOfRecords = numberOfRecordsPerCompounded
					.get(compoundedAttributeType);
			if (null == numberOfRecords) {
				// long live auto-boxing NPEs... big step forward
				numberOfRecords = 1L;
			}
			if (numberOfRecords < attributeIndex + 1) {
				numberOfRecords = attributeIndex + 1;
			}
			numberOfRecordsPerCompounded.put(compoundedAttributeType,
					numberOfRecords);
		}

		for (Map.Entry<AttributeTypeEntity, Map<AttributeTypeEntity, Map<Long, AttributeEntity>>> compoundedAttributeEntry : compoundedAttributes
				.entrySet()) {
			AttributeTypeEntity compoundedAttributeType = compoundedAttributeEntry
					.getKey();
			Map<AttributeTypeEntity, Map<Long, AttributeEntity>> membersMap = compoundedAttributeEntry
					.getValue();

			String humanReadableName = null;
			String description = null;
			if (null != language) {
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(
								compoundedAttributeType.getName(), language));
				if (null != attributeTypeDescription) {
					LOG.debug("found description");
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}

			long numberOfRecords = numberOfRecordsPerCompounded
					.get(compoundedAttributeType);
			for (long idx = 0; idx < numberOfRecords; idx++) {
				AttributeDO compoundedAttributeView = new AttributeDO(
						compoundedAttributeType.getName(),
						DatatypeType.COMPOUNDED, true, idx, humanReadableName,
						description, compoundedAttributeType.isUserEditable(),
						false, null, null);
				compoundedAttributeView.setCompounded(true);
				attributesView.add(compoundedAttributeView);

				List<CompoundedAttributeTypeMemberEntity> members = compoundedAttributeType
						.getMembers();
				/*
				 * Remember that the members are in-order.
				 */
				for (CompoundedAttributeTypeMemberEntity member : members) {
					AttributeTypeEntity memberAttributeType = member
							.getMember();

					String memberHumanReadableName = null;
					String memberDescription = null;
					if (null != language) {
						AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
								.findDescription(new AttributeTypeDescriptionPK(
										memberAttributeType.getName(), language));
						if (null != attributeTypeDescription) {
							LOG.debug("found description");
							memberHumanReadableName = attributeTypeDescription
									.getName();
							memberDescription = attributeTypeDescription
									.getDescription();
						}
					}

					AttributeDO attributeView = new AttributeDO(
							memberAttributeType.getName(), memberAttributeType
									.getType(), memberAttributeType
									.isMultivalued(), idx,
							memberHumanReadableName, memberDescription, false,
							false, null, null);
					/*
					 * We mark compounded attribute members as non-editable when
					 * queries via the listAttributes method to ease
					 * visualization.
					 */
					Map<Long, AttributeEntity> attributeValues = membersMap
							.get(memberAttributeType);
					attributeView.setMember(true);
					AttributeEntity attributeValue = null;
					if (null != attributeValues) {
						attributeValue = attributeValues.get(idx);
					}
					if (null != attributeValue) {
						attributeView.setValue(attributeValue);
					}
					attributesView.add(attributeView);
				}
			}
		}

		return attributesView;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean isConfirmationRequired(@NonEmptyString
	String applicationName) throws ApplicationNotFoundException,
			SubscriptionNotFoundException, ApplicationIdentityNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("is confirmation required for application " + applicationName
				+ " by subject " + subject.getUserId());

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
	public void confirmIdentity(@NonEmptyString
	String applicationName) throws ApplicationNotFoundException,
			SubscriptionNotFoundException, ApplicationIdentityNotFoundException {
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

		this.historyDAO.addHistoryEntry(subject,
				HistoryEventType.IDENTITY_CONFIRMATION, applicationName, null);

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
	private void manageIdentityAttributeVisibility(@NotNull
	ApplicationEntity application, @NotNull
	SubjectEntity subject, @NotNull
	SubscriptionEntity subscription)
			throws ApplicationIdentityNotFoundException {
		ApplicationIdentityEntity confirmedApplicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, subscription
						.getConfirmedIdentityVersion());
		LOG.debug("managing identity attribute visibility for version: "
				+ confirmedApplicationIdentity.getIdentityVersion());
		List<AttributeTypeEntity> attributeTypes = confirmedApplicationIdentity
				.getAttributeTypes();
		for (AttributeTypeEntity attributeType : attributeTypes) {
			LOG.debug("checking out attribute existence for "
					+ attributeType.getName());
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
	public List<AttributeDO> listIdentityAttributesToConfirm(@NonEmptyString
	String applicationName, Locale locale) throws ApplicationNotFoundException,
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
	public boolean hasMissingAttributes(@NonEmptyString
	String applicationName) throws ApplicationNotFoundException,
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
			@NonEmptyString
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

	static class RequiredAttributeMapEntryFilter implements
			MapEntryFilter<AttributeTypeEntity, Boolean> {

		public boolean isAllowed(Entry<AttributeTypeEntity, Boolean> element) {
			return element.getValue();
		}
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listMissingAttributes(@NonEmptyString
	String applicationName, Locale locale) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException {
		LOG.debug("get missing attribute for application: " + applicationName);
		Set<AttributeTypeEntity> requiredApplicationAttributeTypes = getRequiredDataAttributeTypes(applicationName);

		SubjectEntity subject = this.subjectManager.getCallerSubject();
		Map<AttributeTypeEntity, List<AttributeEntity>> userAttributes = this.attributeDAO
				.listAttributes(subject);

		for (Map.Entry<AttributeTypeEntity, List<AttributeEntity>> userAttributeEntry : userAttributes
				.entrySet()) {
			AttributeTypeEntity userAttributeType = userAttributeEntry.getKey();
			if (true == userAttributeType.isCompounded()) {
				/*
				 * We don't need to remove a compounded attribute type since
				 * such a type is not part of the
				 * requiredApplicationAttributeTypes list in the first place.
				 */
				continue;
			}
			/*
			 * Even in case of a multi-valued attribute we only need to peek at
			 * the first entry.
			 */
			AttributeEntity userAttribute = userAttributeEntry.getValue()
					.get(0);
			if (true == userAttribute.isEmpty()) {
				continue;
			}
			requiredApplicationAttributeTypes.remove(userAttributeType);
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
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(
								attributeName, language));
				if (null != attributeTypeDescription) {
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			/*
			 * We mark the missing attribute as singled-valued here since
			 * basically the user does not care at this point.
			 */
			AttributeDO missingAttribute = new AttributeDO(attributeName,
					datatype, false, 0, humanReadableName, description,
					attributeType.isUserEditable(), true, null, null);
			LOG.debug("adding missing attribute: " + attributeName);
			missingAttribute.setRequired(true);
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
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(
								compoundedAttributeType.getName(), language));
				if (null != attributeTypeDescription) {
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			AttributeDO missingAttribute = new AttributeDO(
					compoundedAttributeType.getName(), DatatypeType.COMPOUNDED,
					false, 0, humanReadableName, description, false, true,
					null, null);
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
				humanReadableName = null;
				description = null;
				if (null != locale) {
					String language = locale.getLanguage();
					AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
							.findDescription(new AttributeTypeDescriptionPK(
									attributeType.getName(), language));
					if (null != attributeTypeDescription) {
						humanReadableName = attributeTypeDescription.getName();
						description = attributeTypeDescription.getDescription();
					}
				}
				AttributeDO missingMemberAttribute = new AttributeDO(
						attributeType.getName(), attributeType.getType(),
						false, 0, humanReadableName, description, attributeType
								.isUserEditable(), true, null, null);
				missingMemberAttribute.setRequired(member.isRequired());
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
					missingMemberAttribute.setValue(attribute);
				}
				LOG.debug("adding missing member attribute: "
						+ missingMemberAttribute.getName());
				missingAttributes.add(missingMemberAttribute);
			}
		}

		return missingAttributes;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> listConfirmedIdentity(@NonEmptyString
	String applicationName, Locale locale) throws ApplicationNotFoundException,
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
	public List<AttributeDO> listAttributes(@NonEmptyString
	String deviceId, Locale locale) throws DeviceNotFoundException {
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
	public void removeAttribute(@NotNull
	AttributeDO attribute) throws PermissionDeniedException,
			AttributeNotFoundException, AttributeTypeNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String attributeName = attribute.getName();
		LOG.debug("remove attribute " + attributeName
				+ " for entity with login " + subject);
		LOG.debug("received attribute values: " + attribute);

		AttributeTypeEntity attributeType = getUserRemovableAttributeType(attributeName);

		this.attributeManager.removeAttribute(attributeType, attribute
				.getIndex(), subject);

		this.historyDAO.addHistoryEntry(subject,
				HistoryEventType.ATTRIBUTE_REMOVE, attributeName, null);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void addAttribute(@NotNull
	List<AttributeDO> newAttributeContext) throws PermissionDeniedException,
			AttributeTypeNotFoundException {

		AttributeDO headAttribute = newAttributeContext.get(0);
		String attributeName = headAttribute.getName();
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("add attribute " + attributeName + " for entity with login "
				+ subject);

		AttributeTypeEntity attributeType = getUserEditableAttributeType(attributeName);

		boolean multivalued = attributeType.isMultivalued();
		if (false == multivalued) {
			throw new PermissionDeniedException(
					"attribute type is not multivalued");
		}

		if (newAttributeContext.size() > 1) {
			/*
			 * In this case the first entry is the compounded attribute for
			 * which the user wants to create a new record.
			 */
			if (false == attributeType.isCompounded()) {
				throw new PermissionDeniedException(
						"attribute type is not compounded");
			}
			AttributeEntity compoundedAttribute = this.attributeDAO
					.addAttribute(attributeType, subject);
			String compoundedAttributeId = UUID.randomUUID().toString();
			LOG.debug("adding new compounded entry with Id: "
					+ compoundedAttributeId);
			compoundedAttribute.setStringValue(compoundedAttributeId);
			long attributeIndex = compoundedAttribute.getAttributeIndex();
			LOG.debug("compounded attribute index: " + attributeIndex);

			Iterator<AttributeDO> iterator = newAttributeContext
					.listIterator(1);
			while (iterator.hasNext()) {
				AttributeDO attribute = iterator.next();
				if (false == attribute.isEditable()) {
					/*
					 * By skipping this entry we allow an easy handling of a
					 * compounded attribute record in the GUI.
					 */
					continue;
				}
				AttributeTypeEntity memberAttributeType = this.attributeTypeDAO
						.getAttributeType(attribute.getName());
				AttributeEntity memberAttribute = this.attributeDAO
						.addAttribute(memberAttributeType, subject,
								attributeIndex);
				LOG.debug("adding member: " + memberAttributeType.getName());
				attribute.copyValueTo(memberAttributeType, memberAttribute);
			}

			this.historyDAO.addHistoryEntry(subject,
					HistoryEventType.ATTRIBUTE_ADD, attributeName, null);

			return;
		}

		/*
		 * Else we're dealing with a regular multi-valued attribute.
		 */
		AttributeEntity attribute = this.attributeDAO.addAttribute(
				attributeType, subject);
		LOG.debug("new attribute index: " + attribute.getAttributeIndex());
		headAttribute.copyValueTo(attributeType, attribute);
		this.historyDAO.addHistoryEntry(subject,
				HistoryEventType.ATTRIBUTE_ADD, attributeName, null);

	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> getAttributeEditContext(@NotNull
	AttributeDO selectedAttribute) throws AttributeTypeNotFoundException {
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(selectedAttribute.getName());
		if (attributeType.isCompounded()) {
			List<CompoundedAttributeTypeMemberEntity> members = attributeType
					.getMembers();
			SubjectEntity subject = this.subjectManager.getCallerSubject();

			List<AttributeDO> attributeEditContext = new LinkedList<AttributeDO>();
			attributeEditContext.add(selectedAttribute);
			/*
			 * Notice that the members are in-order.
			 */
			long index = selectedAttribute.getIndex();
			for (CompoundedAttributeTypeMemberEntity member : members) {
				AttributeTypeEntity memberAttributeType = member.getMember();
				if (false == memberAttributeType.isUserVisible()) {
					continue;
				}
				AttributeEntity attribute = this.attributeDAO.findAttribute(
						subject, memberAttributeType, index);
				AttributeDO memberView = new AttributeDO(memberAttributeType
						.getName(), memberAttributeType.getType(), true, index,
						null, null, memberAttributeType.isUserEditable(),
						false, null, null);
				memberView.setMember(true);
				if (null != attribute) {
					memberView.setValue(attribute);
				}
				attributeEditContext.add(memberView);
			}

			return attributeEditContext;
		}
		if (attributeType.isCompoundMember()) {
			throw new IllegalArgumentException("cannot handle members itself.");
		}
		/*
		 * Else we're dealing with simple- or multivalued attributes that do not
		 * participate in a compounded record somehow.
		 */
		List<AttributeDO> attributeEditContext = new LinkedList<AttributeDO>();
		attributeEditContext.add(selectedAttribute);
		return attributeEditContext;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AttributeDO> getAttributeTemplate(@NotNull
	AttributeDO prototypeAttribute) throws AttributeTypeNotFoundException {
		String attributeName = prototypeAttribute.getName();
		LOG.debug("getAttributeTemplate: " + attributeName);
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(prototypeAttribute.getName());

		if (attributeType.isCompounded()) {
			List<AttributeDO> attributeTemplate = new LinkedList<AttributeDO>();

			/*
			 * Notice that we mark the entry as single-valued here since we
			 * cannot yet pass a usefull attribute index to the GUI.
			 */
			AttributeDO compoundedAttribute = new AttributeDO(attributeType
					.getName(), attributeType.getType(), false, -1,
					prototypeAttribute.getRawHumanReadableName(),
					prototypeAttribute.getDescription(), attributeType
							.isUserEditable(), false, null, null);
			compoundedAttribute.setCompounded(true);
			attributeTemplate.add(compoundedAttribute);

			List<CompoundedAttributeTypeMemberEntity> members = attributeType
					.getMembers();

			for (CompoundedAttributeTypeMemberEntity member : members) {
				AttributeTypeEntity memberAttributeType = member.getMember();

				/*
				 * Notice that we mark the entry as single-valued here since we
				 * cannot yet pass a usefull attribute index to the GUI.
				 */
				AttributeDO memberAttribute = new AttributeDO(
						memberAttributeType.getName(), memberAttributeType
								.getType(), false, -1, null, null,
						memberAttributeType.isUserEditable(), false, null, null);
				memberAttribute.setMember(true);
				attributeTemplate.add(memberAttribute);
			}

			return attributeTemplate;
		}

		if (attributeType.isCompoundMember()) {
			throw new IllegalArgumentException(
					"cannot handle compounded members itself");
		}

		/*
		 * Notice that we mark the entry as single-valued here since we cannot
		 * yet pass a usefull attribute index to the GUI.
		 */
		AttributeDO attribute = new AttributeDO(attributeType.getName(),
				attributeType.getType(), false, -1, prototypeAttribute
						.getRawHumanReadableName(), prototypeAttribute
						.getDescription(), attributeType.isUserEditable(),
				false, null, null);
		List<AttributeDO> attributeTemplate = new LinkedList<AttributeDO>();
		attributeTemplate.add(attribute);
		return attributeTemplate;
	}
}
