/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.security.DenyAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.data.AccountMergingDO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.ChoosableAttributeDO;
import net.link.safeonline.data.SubscriptionDO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.service.AccountMergingService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = AccountMergingService.JNDI_BINDING)
public class AccountMergingServiceBean implements AccountMergingService {

    private static final Log       LOG = LogFactory.getLog(AccountMergingServiceBean.class);

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = SubjectManager.JNDI_BINDING)
    private SubjectManager         subjectManager;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    @EJB(mappedName = SubscriptionDAO.JNDI_BINDING)
    private SubscriptionDAO        subscriptionDAO;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService    devicePolicyService;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = AccountService.JNDI_BINDING)
    private AccountService         accountService;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        LOG.debug("postConstruct");
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    /**
     * Dry run of merging with the specified account. Returns an account merging data object.
     * 
     * @throws AttributeNotFoundException
     * 
     */
    @DenyAll
    public AccountMergingDO getAccountMergingDO(String sourceAccountName)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, ApplicationNotFoundException, EmptyDevicePolicyException,
            AttributeNotFoundException {

        LOG.debug("merge account: " + sourceAccountName);
        SubjectEntity targetSubject = subjectManager.getCallerSubject();
        SubjectEntity sourceSubject = subjectService.getSubjectFromUserName(sourceAccountName);

        AccountMergingDO accountMergingDO = new AccountMergingDO(sourceSubject);

        List<SubscriptionEntity> targetSubscriptions = subscriptionDAO.listSubsciptions(targetSubject);
        List<SubscriptionEntity> sourceSubscriptions = subscriptionDAO.listSubsciptions(sourceSubject);
        mergeSubscriptions(accountMergingDO, targetSubscriptions, sourceSubscriptions);

        Map<AttributeTypeEntity, List<AttributeEntity>> targetAttributes = attributeDAO.listAttributes(targetSubject);
        Map<AttributeTypeEntity, List<AttributeEntity>> sourceAttributes = attributeDAO.listAttributes(sourceSubject);
        mergeAttributes(accountMergingDO, targetAttributes, sourceAttributes);

        return accountMergingDO;
    }

    /**
     * Commits a merge given an account merging data object.
     * 
     * @throws SubjectNotFoundException
     * @throws PermissionDeniedException
     * @throws SubscriptionNotFoundException
     * @throws MessageHandlerNotFoundException
     */
    @DenyAll
    public void mergeAccount(AccountMergingDO accountMergingDO, Set<DeviceEntity> neededDevices)
            throws AttributeTypeNotFoundException, SubjectNotFoundException, PermissionDeniedException, SubscriptionNotFoundException,
            MessageHandlerNotFoundException {

        LOG.debug("commit merge with account " + accountMergingDO.getSourceSubject().getUserId());
        if (null != neededDevices && neededDevices.size() != 0)
            throw new PermissionDeniedException("authentication needed for certain devices");
        SubjectEntity targetSubject = subjectManager.getCallerSubject();
        SubjectEntity sourceSubject = subjectService.getSubject(accountMergingDO.getSourceSubject().getUserId());
        /*
         * Add attributes
         */
        commitMerge(accountMergingDO.getMergedAttributesToAdd());
        commitMerge(accountMergingDO.getImportedAttributes());

        if (null != accountMergingDO.getChoosableAttributes()) {
            List<AttributeDO> chosenSourceAttributes = new LinkedList<AttributeDO>();
            for (ChoosableAttributeDO choosableAttribute : accountMergingDO.getChoosableAttributes()) {
                if (choosableAttribute.isSourceSelected()) {
                    chosenSourceAttributes.add(choosableAttribute.getSourceAttribute());
                }
            }
            commitMerge(chosenSourceAttributes);
        }
        /*
         * Update subject identifiers
         */
        List<SubjectIdentifierEntity> sourceSubjectIdentifiers = subjectIdentifierDAO.getSubjectIdentifiers(sourceSubject);
        for (SubjectIdentifierEntity subjectIdentifier : sourceSubjectIdentifiers) {
            subjectIdentifier.setSubject(targetSubject);
        }

        /*
         * Remove the remaining subject identifier in the login domain
         */
        String targetSubjectLogin = subjectService.getSubjectLogin(targetSubject.getUserId());
        subjectIdentifierDAO.removeOtherSubjectIdentifiers(SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN, targetSubjectLogin, targetSubject);
        /*
         * Remove source account, without removing the subject identifiers.
         */
        accountService.removeAccount(sourceSubject.getUserId());

        /*
         * Update subscriptions
         */
        if (null != accountMergingDO.getImportedSubscriptions()) {
            for (SubscriptionDO importingSubscription : accountMergingDO.getImportedSubscriptions()) {
                subscriptionDAO.addSubscription(importingSubscription.getSubscription().getSubscriptionOwnerType(), targetSubject,
                        importingSubscription.getSubscription().getApplication(), importingSubscription.getSubscription()
                                                                                                       .getSubscriptionUserId());
            }
        }

    }

    /**
     * Adds a list of attributes
     * 
     * @param attributes
     * @throws AttributeTypeNotFoundException
     */
    private void commitMerge(List<AttributeDO> attributes)
            throws AttributeTypeNotFoundException {

        SubjectEntity subject = subjectManager.getCallerSubject();
        Iterator<AttributeDO> iterator = attributes.listIterator();
        while (iterator.hasNext()) {
            AttributeDO attribute = iterator.next();
            LOG.debug("add attribute: " + attribute.getName());
            AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attribute.getName());
            AttributeEntity attributeEntity = attributeDAO.addAttribute(attributeType, subject);
            if (attributeType.isCompounded()) {
                attributeEntity.setValue(attribute.getStringValue());
            } else {
                attribute.copyValueTo(attributeType, attributeEntity);
            }
        }
    }

    /**
     * Merge subscriptions :
     * 
     * When a subscription does already exist in the target: do not move
     * 
     * When a subscription does not yet exist: move if the source account holder has proven possession of a device from the allowed devices
     * list of the application. When there is no allowed devices list: any device is sufficient.
     * 
     * @param targetSubscriptions
     * @param sourceSubscriptions
     * @throws EmptyDevicePolicyException
     * @throws ApplicationNotFoundException
     */
    private void mergeSubscriptions(AccountMergingDO accountMergingDO, List<SubscriptionEntity> targetSubscriptions,
                                    List<SubscriptionEntity> sourceSubscriptions)
            throws ApplicationNotFoundException, EmptyDevicePolicyException {

        accountMergingDO.setPreservedSubscriptions(targetSubscriptions);
        for (SubscriptionEntity sourceSubscription : sourceSubscriptions) {
            SubscriptionEntity targetSubscription = getSubscription(targetSubscriptions, sourceSubscription.getApplication());
            if (null == targetSubscription) {
                SubscriptionDO subscriptionDO = getSubscriptionDO(sourceSubscription);
                accountMergingDO.addImportedSubscription(subscriptionDO);
                if (null != subscriptionDO.getAllowedDevices()) {
                    accountMergingDO.addNeededProvenDevices(subscriptionDO.getAllowedDevices());
                }
            }
        }
    }

    private SubscriptionEntity getSubscription(List<SubscriptionEntity> subscriptions, ApplicationEntity application) {

        for (SubscriptionEntity subscription : subscriptions) {
            if (subscription.getApplication().equals(application))
                return subscription;
        }
        return null;
    }

    private SubscriptionDO getSubscriptionDO(SubscriptionEntity subscription)
            throws ApplicationNotFoundException, EmptyDevicePolicyException {

        List<DeviceEntity> allowedDevices = null;
        if (subscription.getApplication().isDeviceRestriction()) {
            allowedDevices = devicePolicyService.getDevicePolicy(subscription.getApplication().getId(), null);
        }
        return new SubscriptionDO(subscription, allowedDevices);
    }

    /**
     * Merge attributes
     * 
     * no corresponding attribute exists: move to target
     * 
     * multivalued attributes: add values to target
     * 
     * single valued, user editable: user can choose
     * 
     * single valued, non user editable: use target attributes.
     * 
     * @param targetAttributes
     * @param sourceAttributes
     * @throws AttributeNotFoundException
     * @throws AttributeTypeNotFoundException
     */
    private void mergeAttributes(AccountMergingDO accountMergingDO, Map<AttributeTypeEntity, List<AttributeEntity>> targetAttributes,
                                 Map<AttributeTypeEntity, List<AttributeEntity>> sourceAttributes)
            throws AttributeNotFoundException {

        Map<AttributeTypeEntity, List<AttributeEntity>> preservedAttributeMap = new HashMap<AttributeTypeEntity, List<AttributeEntity>>(
                targetAttributes);
        List<AttributeEntity> importedAttributesList = new LinkedList<AttributeEntity>();
        List<AttributeEntity> mergedAttributesList = new LinkedList<AttributeEntity>();
        List<AttributeEntity> mergedAttributesToAddList = new LinkedList<AttributeEntity>();
        Map<List<AttributeEntity>, List<AttributeEntity>> choosableAttributesMap = new HashMap<List<AttributeEntity>, List<AttributeEntity>>();

        for (Entry<AttributeTypeEntity, List<AttributeEntity>> sourceAttribute : sourceAttributes.entrySet()) {
            AttributeTypeEntity sourceAttributeType = sourceAttribute.getKey();
            LOG.debug("handling attribute: " + sourceAttributeType.getName() + " ( " + sourceAttributeType.getType() + " ) : size="
                    + sourceAttribute.getValue().size());
            Entry<AttributeTypeEntity, List<AttributeEntity>> targetAttribute = getAttribute(targetAttributes, sourceAttributeType);
            if (null == targetAttribute) {
                importedAttributesList.addAll(sourceAttribute.getValue());
            } else if (sourceAttributeType.isMultivalued()) {
                List<AttributeEntity> mergedAttributeToAddList = new LinkedList<AttributeEntity>();
                /*
                 * compounded attributes should be merged as one big attribute
                 */
                if (sourceAttributeType.getType().equals(DatatypeType.COMPOUNDED)) {
                    List<AttributeEntity> mergedCompoundedAttributesToAdd = mergeCompoundedAttribute(targetAttribute, sourceAttribute);
                    for (AttributeEntity mergedCompoundedAttributeToAdd : mergedCompoundedAttributesToAdd) {
                        mergedAttributeToAddList.add(mergedCompoundedAttributeToAdd);
                        List<AttributeEntity> mergedCompoundedMembers = attributeManager.getCompoundMembers(mergedCompoundedAttributeToAdd);
                        for (AttributeEntity mergedCompoundedMember : mergedCompoundedMembers) {
                            mergedAttributeToAddList.add(mergedCompoundedMember);
                        }
                    }
                }
                /*
                 * dealt with when the parent compounded attribute is handled
                 */
                else if (sourceAttributeType.isCompoundMember()) {
                    preservedAttributeMap.remove(targetAttribute.getKey());
                    continue;
                } else {
                    mergedAttributeToAddList = mergeAttribute(targetAttribute, sourceAttribute);
                }
                targetAttribute.getValue().addAll(mergedAttributeToAddList);
                mergedAttributesList.addAll(targetAttribute.getValue());
                mergedAttributesToAddList.addAll(mergedAttributeToAddList);
                preservedAttributeMap.remove(targetAttribute.getKey());
            } else if (sourceAttributeType.isUserEditable()) {
                choosableAttributesMap.put(targetAttribute.getValue(), sourceAttribute.getValue());
                preservedAttributeMap.remove(targetAttribute.getKey());
            }
        }

        /*
         * now convert the resulted lists/maps to AttributeDO's for the presentation layer
         */
        for (Entry<AttributeTypeEntity, List<AttributeEntity>> preservedAttribute : preservedAttributeMap.entrySet()) {
            List<AttributeEntity> preservedAttributes = preservedAttribute.getValue();
            accountMergingDO.addPreservedAttributes(mapAttributes(preservedAttributes, null));
        }
        accountMergingDO.setImportedAttributes(mapAttributes(importedAttributesList, null));
        accountMergingDO.setMergedAttributes(mapAttributes(mergedAttributesList, null));
        accountMergingDO.setMergedAttributesToAdd(mapAttributes(mergedAttributesToAddList, null));
        for (Entry<List<AttributeEntity>, List<AttributeEntity>> choosableAttribute : choosableAttributesMap.entrySet()) {
            accountMergingDO.addChoosableAttributes(mapAttributes(choosableAttribute.getKey(), null), mapAttributes(
                    choosableAttribute.getValue(), null));
        }
    }

    /**
     * Merge two compounded multivalued attributes, removing doubles.
     * 
     * for each compound ( source and target ) : fetch all its members in a list then compare all attributes in these lists with each other
     * ...
     * 
     * Returns only the list of attributes to be added to the target attribute
     * 
     * @param targetAttributes
     * @param sourceAttributes
     * @throws AttributeNotFoundException
     */
    private List<AttributeEntity> mergeCompoundedAttribute(Entry<AttributeTypeEntity, List<AttributeEntity>> targetAttributes,
                                                           Entry<AttributeTypeEntity, List<AttributeEntity>> sourceAttributes)
            throws AttributeNotFoundException {

        /*
         * lets merge
         */
        List<AttributeEntity> mergedAttributesToAdd = new LinkedList<AttributeEntity>();
        for (AttributeEntity sourceAttribute : sourceAttributes.getValue()) {
            List<AttributeEntity> sourceMembers = attributeManager.getCompoundMembers(sourceAttribute);
            boolean found = false;
            for (AttributeEntity targetAttribute : targetAttributes.getValue()) {
                List<AttributeEntity> targetMembers = attributeManager.getCompoundMembers(targetAttribute);
                if (compoundEqual(sourceMembers, targetMembers)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                sourceAttribute.setAttributeIndex(targetAttributes.getValue().size() + mergedAttributesToAdd.size());
                for (AttributeEntity sourceMember : sourceMembers) {
                    sourceMember.setAttributeIndex(sourceAttribute.getAttributeIndex());
                }
                mergedAttributesToAdd.add(sourceAttribute);
            }
        }
        return mergedAttributesToAdd;
    }

    /**
     * Compares the attribute members of one compounded attribute to another one
     * 
     * @param sourceMembers
     * @param targetMembers
     */
    private boolean compoundEqual(List<AttributeEntity> sourceMembers, List<AttributeEntity> targetMembers) {

        for (AttributeEntity sourceMember : sourceMembers) {
            if (!getAttributeValue(targetMembers, sourceMember))
                return false;
        }
        return true;
    }

    /**
     * Merges two multivalued attributes, removing doubles. Compounded attributes are not handled here.
     * 
     * Returns the list of attributes that will need to be added.
     * 
     * @param sourceAttribute
     * @param targetAttribute
     */
    private List<AttributeEntity> mergeAttribute(Entry<AttributeTypeEntity, List<AttributeEntity>> targetAttribute,
                                                 Entry<AttributeTypeEntity, List<AttributeEntity>> sourceAttribute) {

        List<AttributeEntity> attributesToAddList = new LinkedList<AttributeEntity>();
        for (AttributeEntity attributeValue : sourceAttribute.getValue()) {
            if (!getAttributeValue(targetAttribute.getValue(), attributeValue)) {
                attributeValue.setAttributeIndex(targetAttribute.getValue().size() + attributesToAddList.size());
                attributesToAddList.add(attributeValue);
            }
        }
        return attributesToAddList;
    }

    private boolean getAttributeValue(List<AttributeEntity> attributeValues, AttributeEntity searchAttributeValue) {

        for (AttributeEntity attributeValue : attributeValues) {
            if (attributeValue.getValue().equals(searchAttributeValue.getValue()))
                return true;
        }
        return false;
    }

    private Entry<AttributeTypeEntity, List<AttributeEntity>> getAttribute(Map<AttributeTypeEntity, List<AttributeEntity>> attributes,
                                                                           AttributeTypeEntity attributeType) {

        for (Entry<AttributeTypeEntity, List<AttributeEntity>> attribute : attributes.entrySet()) {
            if (attribute.getKey().equals(attributeType))
                return attribute;
        }
        return null;
    }

    private List<AttributeDO> mapAttributes(List<AttributeEntity> attributes, String language) {

        List<AttributeDO> attributesView = new LinkedList<AttributeDO>();
        for (AttributeEntity attribute : attributes) {
            AttributeTypeEntity attributeType = attribute.getAttributeType();

            // if language is given, try to fetch human readable name and
            // description
            String humanReadableName = null;
            String description = null;
            if (null != language) {
                LOG.debug("trying language: " + language);
                AttributeTypeDescriptionEntity attributeTypeDescription = attributeTypeDAO.findDescription(new AttributeTypeDescriptionPK(
                        attributeType.getName(), language));
                if (null != attributeTypeDescription) {
                    LOG.debug("found description");
                    humanReadableName = attributeTypeDescription.getName();
                    description = attributeTypeDescription.getDescription();
                }
            }

            AttributeDO attributeView = new AttributeDO(attributeType.getName(), attributeType.getType(), attributeType.isMultivalued(),
                    attribute.getAttributeIndex(), humanReadableName, description, attributeType.isUserEditable(), true,
                    attribute.getStringValue(), attribute.getBooleanValue());
            if (!attributeType.isCompounded()) {
                attributeView.setValue(attribute);
            }
            attributeView.setUserVisible(attributeType.isUserVisible());
            attributeView.setCompounded(attributeType.isCompounded());
            attributeView.setMember(attributeType.isCompoundMember());
            attributesView.add(attributeView);

        }
        return attributesView;
    }
}
