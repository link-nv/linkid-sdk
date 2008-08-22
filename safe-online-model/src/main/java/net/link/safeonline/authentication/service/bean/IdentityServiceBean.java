/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.Collections;
import java.util.HashMap;
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
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.IdentityServiceRemote;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.data.AttributeDO;
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
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, InputValidation.class })
public class IdentityServiceBean implements IdentityService, IdentityServiceRemote {

    static final Log                          LOG = LogFactory.getLog(IdentityServiceBean.class);

    @EJB
    private SubjectManager                    subjectManager;

    @EJB
    private HistoryDAO                        historyDAO;

    @EJB
    private AttributeDAO                      attributeDAO;

    @EJB
    private AttributeTypeDAO                  attributeTypeDAO;

    @EJB
    private ApplicationDAO                    applicationDAO;

    @EJB
    private SubscriptionDAO                   subscriptionDAO;

    @EJB
    private ApplicationIdentityDAO            applicationIdentityDAO;

    @EJB
    private AttributeTypeDescriptionDecorator attributeTypeDescriptionDecorator;

    @EJB
    private ProxyAttributeService             proxyAttributeService;

    @EJB
    private DeviceDAO                         deviceDAO;

    private AttributeManagerLWBean            attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute
         * manager (a lightweight bean) will live within the same transaction and security context as this identity
         * service EJB3 session bean.
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

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<HistoryEntity> listHistory(SubjectEntity subject) {

        return this.historyDAO.getHistory(subject);
    }

    /**
     * Gives back the attribute type for the given attribute name, but only if the user is allowed to edit attributes of
     * the attribute type.
     * 
     * @param attributeName
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     */
    private AttributeTypeEntity getUserEditableAttributeType(@NonEmptyString String attributeName)
            throws PermissionDeniedException, AttributeTypeNotFoundException {

        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeName);
        if (false == attributeType.isUserEditable()) {
            LOG.debug("user not allowed to edit attribute of type: " + attributeName);
            throw new PermissionDeniedException("user not allowed to edit attribute of type: " + attributeName);
        }
        return attributeType;
    }

    /**
     * Gives back the attribute type for the given attribute name, but only if the user is allowed to remove attributes
     * of the attribute type.
     * 
     * @param attributeName
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     */
    private AttributeTypeEntity getUserRemovableAttributeType(@NonEmptyString String attributeName)
            throws PermissionDeniedException, AttributeTypeNotFoundException {

        AttributeTypeEntity attributeType = this.attributeTypeDAO.findAttributeType(attributeName);
        if (null == attributeType)
            throw new IllegalArgumentException("attribute type not found: " + attributeName);
        if (true == attributeType.isUserEditable())
            return attributeType;
        if (false == attributeType.isCompoundMember()) {
            String msg = "attribute type is not a compounded member: " + attributeType.getName();
            LOG.debug(msg);
            throw new PermissionDeniedException(msg);
        }
        /*
         * We make an exception here for compounded member attributes here. Even if the member attribute type is marked
         * as being non-user-editable the user is allowed to remove the entry if the compounded attribute type is
         * editable.
         */
        AttributeTypeEntity compoundedAttributeType = this.attributeTypeDAO.getParent(attributeType);
        if (true == compoundedAttributeType.isUserEditable())
            return attributeType;
        String msg = "compounded parent attribute type is not user editable: " + compoundedAttributeType.getName();
        LOG.debug(msg);
        throw new PermissionDeniedException(msg);
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void saveAttribute(@NotNull AttributeDO attribute) throws PermissionDeniedException,
            AttributeTypeNotFoundException {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        String attributeName = attribute.getName();
        long index = attribute.getIndex();
        LOG.debug("save attribute " + attributeName + " for entity with login " + subject + "; index " + index);
        LOG.debug("received attribute value: " + attribute);

        if (attribute.isCompounded()) {
            LOG.debug("save compounded attribute");
            /*
             * A compounded attribute record has a top-level attribute entry containing a UUID to uniquely identify the
             * compounded attribute record.
             */
            AttributeTypeEntity compoundedAttributeType = getUserEditableAttributeType(attributeName);
            AttributeEntity compoundedAttribute = this.attributeDAO.findAttribute(subject, compoundedAttributeType,
                    index);
            if (null == compoundedAttribute) {
                /*
                 * This situation is possible when filling in a compounded attribute record during the missing
                 * attributes phase of the authentication process.
                 */
                compoundedAttribute = this.attributeDAO.addAttribute(compoundedAttributeType, subject, index);
                String compoundedAttributeId = UUID.randomUUID().toString();
                LOG.debug("adding compounded attribute for " + subject.getUserId() + " of type " + attributeName
                        + " with ID " + compoundedAttributeId);
                compoundedAttribute.setStringValue(compoundedAttributeId);
            }
            /*
             * Notice that, if there is already a compounded attribute for the given record index, then we don't
             * overwrite it with a new ID. The idea behind the ID is that it remains constant during the lifecycle of
             * the compounded attribute record.
             */
            return;
        }

        if (false == attribute.isEditable()) {
            /*
             * We allow the web application to pass in saveAttribute calls with attributes marked as non-editable, that
             * way we have a transparent handling of attributes in the GUI.
             */
            LOG.debug("attribute marked as non-editable; skipping");
            return;
        }

        AttributeTypeEntity attributeType = getUserEditableAttributeType(attributeName);

        boolean multiValued = attributeType.isMultivalued();
        if (false == multiValued) {
            if (0 != index)
                throw new IllegalArgumentException("index cannot <> 0 on single-valued attribute type");
        }

        DatatypeType type = attributeType.getType();
        if (attribute.getType() != type)
            throw new EJBException("datatype does not match");

        AttributeEntity attributeEntity = this.attributeDAO.findAttribute(subject, attributeType, index);
        if (null == attributeEntity) {
            attributeEntity = this.attributeDAO.addAttribute(attributeType, subject, index);
        }
        attribute.copyValueTo(attributeType, attributeEntity);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.ATTRIBUTE_CHANGE, Collections.singletonMap(
                SafeOnlineConstants.ATTRIBUTE_PROPERTY, attributeName));
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AttributeDO> listAttributes(@NotNull SubjectEntity subject, Locale locale)
            throws PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("get attributes for " + subject.getUserId());

        List<AttributeTypeEntity> attributeTypes = this.attributeTypeDAO.listAttributeTypes();
        return listAttributes(subject, attributeTypes, locale, true);
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> listAttributes(Locale locale) throws AttributeTypeNotFoundException,
            PermissionDeniedException, ApplicationIdentityNotFoundException {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        LOG.debug("get attributes for " + subject.getUserId());

        List<AttributeTypeEntity> attributeTypes = new LinkedList<AttributeTypeEntity>();
        List<AttributeDO> attributes = new LinkedList<AttributeDO>();

        List<SubscriptionEntity> subscriptions = this.subscriptionDAO.listSubsciptions(subject);
        for (SubscriptionEntity subscription : subscriptions) {
            if (null != subscription.getConfirmedIdentityVersion()) {
                LOG.debug("get attributes for application: " + subscription.getApplication().getName());
                ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO.getApplicationIdentity(
                        subscription.getApplication(), subscription.getConfirmedIdentityVersion());
                for (ApplicationIdentityAttributeEntity identityAttribute : applicationIdentity.getAttributes()) {
                    if (identityAttribute.getAttributeType().isUserVisible()) {
                        attributeTypes.add(identityAttribute.getAttributeType());
                    }
                }
            }
        }
        attributes.addAll(listAttributes(subject, attributeTypes, locale, true));

        attributeTypes.clear();
        List<DeviceEntity> devices = this.deviceDAO.listDevices();
        for (DeviceEntity device : devices) {
            if (null != device.getAttributeType() && device.getAttributeType().isUserVisible()) {
                LOG.debug("add device attribute type: " + device.getAttributeType());
                attributeTypes.add(device.getAttributeType());
            }
            if (null != device.getUserAttributeType() && device.getUserAttributeType().isUserVisible()
                    && !device.getAttributeType().equals(device.getUserAttributeType())) {
                LOG.debug("add device user attribute type: " + device.getUserAttributeType());
                attributeTypes.add(device.getUserAttributeType());
            }
        }
        attributes.addAll(listAttributes(subject, attributeTypes, locale, false));
        return attributes;
    }

    /**
     * Returns list of attribute data objects given the subject and the list of attribute types.
     */
    private List<AttributeDO> listAttributes(SubjectEntity subject, List<AttributeTypeEntity> attributeTypes,
            Locale locale, boolean addTemplate) throws PermissionDeniedException, AttributeTypeNotFoundException {

        List<AttributeDO> attributesView = new LinkedList<AttributeDO>();
        for (AttributeTypeEntity attributeType : attributeTypes) {
            // get these with their parent, skip
            if (attributeType.isCompoundMember()) {
                continue;
            }
            LOG.debug("find attribute value for type: " + attributeType.getName());
            Object value = this.proxyAttributeService.findAttributeValue(subject.getUserId(), attributeType.getName());
            // No value found so this must be an optional attribute, add a
            // template attribute view.
            if (null == value && addTemplate) {
                addTemplateToView(attributeType, attributesView, locale, false);
                continue;
            } else if (null == value) {
                continue;
            }
            addValueToView(value, attributeType, attributesView, locale);
        }
        return attributesView;
    }

    private void addTemplateToView(AttributeTypeEntity attributeType, List<AttributeDO> attributesView, Locale locale,
            boolean missingAttribute) {

        LOG.debug("add template attribute " + attributeType.getName() + " to view");
        if (!attributeType.isMultivalued() && !attributeType.isCompounded()) {
            // single or multi-valued but NOT compounded
            attributesView.add(getAttributeView(attributeType, null, 0, locale, missingAttribute));
        } else {
            // compounded
            attributesView.add(getAttributeView(attributeType, null, 0, locale, missingAttribute)); // parent
            for (CompoundedAttributeTypeMemberEntity memberAttributeType : attributeType.getMembers()) {
                LOG.debug("add compounded member attribute template: " + memberAttributeType.getMember().getName());
                attributesView
                        .add(getAttributeView(memberAttributeType.getMember(), null, 0, locale, missingAttribute));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addValueToView(Object value, AttributeTypeEntity attributeType, List<AttributeDO> attributesView,
            Locale locale) {

        LOG.debug("add attribute " + attributeType.getName() + " to view");
        if (!attributeType.isMultivalued()) {
            // single-valued
            attributesView.add(getAttributeView(attributeType, value, 0, locale, false));
        } else if (!attributeType.isCompounded()) {
            // multi-valued but NOT compounded
            int idx = 0;
            for (Object attributeValue : (Object[]) value) {
                attributesView.add(getAttributeView(attributeType, attributeValue, idx, locale, false));
                idx++;
            }
        } else {
            // compounded
            int idx = 0;
            for (Object attributeValue : (Object[]) value) {
                Map<String, Object> memberMap = (Map<String, Object>) attributeValue;
                // first add an attribute view for the parent attribute
                // type
                LOG.debug("add compounded attribute: " + attributeType.getName());
                attributesView.add(getAttributeView(attributeType, null, idx, locale, false));
                for (CompoundedAttributeTypeMemberEntity memberAttributeType : attributeType.getMembers()) {
                    LOG.debug("add compounded member attribute: " + memberAttributeType.getMember().getName());
                    attributesView.add(getAttributeView(memberAttributeType.getMember(), memberMap
                            .get(memberAttributeType.getMember().getName()), idx, locale, false));
                }
                idx++;
            }
        }
    }

    /**
     * Returns an attribute view for the given attribute. If value is specified, it must be a single attribute at this
     * point, not a multi-valued or compounded attribute. The value can be null in case of a compounded parent
     * attribute, or if a template view is wanted. missingAttribute is used to determine if compounded member attributes
     * should be editable or not.
     */
    private AttributeDO getAttributeView(AttributeTypeEntity attributeType, Object value, int idx, Locale locale,
            boolean missingAttribute) {

        LOG.debug("get attribute view for type: " + attributeType.getName() + " with value: " + value);
        String humanReadableName = null;
        String description = null;
        AttributeTypeDescriptionEntity attributeTypeDescription = findAttributeTypeDescription(attributeType, locale);
        if (null != attributeTypeDescription) {
            humanReadableName = attributeTypeDescription.getName();
            description = attributeTypeDescription.getDescription();
        }
        AttributeDO attributeView = new AttributeDO(attributeType.getName(), attributeType.getType(), attributeType
                .isMultivalued(), idx, humanReadableName, description, attributeType.isUserEditable(), false, null,
                null);

        attributeView.setCompounded(attributeType.isCompounded());
        attributeView.setMember(attributeType.isCompoundMember());
        /*
         * We mark compounded attribute members as non-editable when queries via the listAttributes method to ease
         * visualization. This is not the case if we are making a view for the missing attributes page, then the user
         * editable'ness should be as set in the attribute type.
         */
        if (attributeType.isCompoundMember() && !missingAttribute) {
            attributeView.setEditable(false);
        }

        if (null != value) {
            attributeView.setValue(value);
        }
        return attributeView;
    }

    private AttributeTypeDescriptionEntity findAttributeTypeDescription(AttributeTypeEntity attributeType, Locale locale) {

        if (null == locale)
            return null;
        String language = locale.getLanguage();
        LOG.debug("trying language: " + language);
        AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
                .findDescription(new AttributeTypeDescriptionPK(attributeType.getName(), language));
        return attributeTypeDescription;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public boolean isConfirmationRequired(@NonEmptyString String applicationName) throws ApplicationNotFoundException,
            SubscriptionNotFoundException, ApplicationIdentityNotFoundException {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        LOG.debug("is confirmation required for application " + applicationName + " by subject " + subject.getUserId());

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        long currentIdentityVersion = application.getCurrentApplicationIdentity();
        ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO.getApplicationIdentity(application,
                currentIdentityVersion);
        Set<ApplicationIdentityAttributeEntity> identityAttributeTypes = applicationIdentity.getAttributes();
        if (true == identityAttributeTypes.isEmpty())
            /*
             * If the identity is empty, the user does not need to do the explicit confirmation.
             */
            return false;

        SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(subject, application);
        if (null == subscription.getConfirmedIdentityVersion())
            /*
             * In this case the user did not yet confirm any identity version yet.
             */
            return true;

        long confirmedIdentityVersion = subscription.getConfirmedIdentityVersion();

        if (currentIdentityVersion != confirmedIdentityVersion)
            return true;
        return false;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void confirmIdentity(@NonEmptyString String applicationName) throws ApplicationNotFoundException,
            SubscriptionNotFoundException, ApplicationIdentityNotFoundException {

        LOG.debug("confirm identity for application: " + applicationName);

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        long currentApplicationIdentityVersion = application.getCurrentApplicationIdentity();

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(subject, application);

        subscription.setConfirmedIdentityVersion(currentApplicationIdentityVersion);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.IDENTITY_CONFIRMATION, Collections.singletonMap(
                SafeOnlineConstants.APPLICATION_PROPERTY, applicationName));

    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> listIdentityAttributesToConfirm(@NonEmptyString String applicationName, Locale locale)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, SubscriptionNotFoundException {

        LOG.debug("get identity to confirm for application: " + applicationName);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        long currentApplicationIdentityVersion = application.getCurrentApplicationIdentity();
        ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO.getApplicationIdentity(application,
                currentApplicationIdentityVersion);
        Set<ApplicationIdentityAttributeEntity> currentIdentityAttributes = applicationIdentity.getAttributes();

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(subject, application);
        Long confirmedIdentityVersion = subscription.getConfirmedIdentityVersion();

        if (null == confirmedIdentityVersion) {
            /*
             * If no identity version was confirmed previously, then the user needs to confirm the current application
             * identity attributes.
             */
            LOG.debug("currentIdentityAttributes: " + currentIdentityAttributes);
            List<AttributeDO> resultAttributes = this.attributeTypeDescriptionDecorator
                    .addDescriptionFromIdentityAttributes(currentIdentityAttributes, locale);
            return resultAttributes;
        }

        // fetch the attribute types that are already agreed upon
        ApplicationIdentityEntity confirmedApplicationIdentity = this.applicationIdentityDAO.getApplicationIdentity(
                application, confirmedIdentityVersion);
        Set<ApplicationIdentityAttributeEntity> confirmedAttributeTypes = confirmedApplicationIdentity.getAttributes();

        List<ApplicationIdentityAttributeEntity> toConfirmAttributes = new LinkedList<ApplicationIdentityAttributeEntity>();
        toConfirmAttributes.addAll(currentIdentityAttributes);
        /*
         * Be careful here not to edit the currentIdentityAttributeTypes list itself.
         */
        for (ApplicationIdentityAttributeEntity target : confirmedAttributeTypes) {
            for (ApplicationIdentityAttributeEntity current : toConfirmAttributes) {
                if (target.equivalent(current)) {
                    toConfirmAttributes.remove(current);
                    break;
                }
            }
        }

        List<AttributeDO> resultAttributes = this.attributeTypeDescriptionDecorator
                .addDescriptionFromIdentityAttributes(toConfirmAttributes, locale);
        return resultAttributes;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public boolean hasMissingAttributes(@NonEmptyString String applicationName) throws ApplicationNotFoundException,
            ApplicationIdentityNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("hasMissingAttributes for application: " + applicationName);
        List<AttributeDO> missingAttributes = listMissingAttributes(applicationName, null);
        return false == missingAttributes.isEmpty();
    }

    /**
     * Gives back all the data attribute types for the given application, required or optional as specified. This method
     * will also expand compounded attribute types.
     * 
     * @param applicationName
     * @throws ApplicationNotFoundException
     * @throws ApplicationIdentityNotFoundException
     */
    private List<AttributeTypeEntity> getDataAttributeTypes(@NonEmptyString String applicationName, boolean required)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        long currentApplicationIdentityVersion = application.getCurrentApplicationIdentity();
        ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO.getApplicationIdentity(application,
                currentApplicationIdentityVersion);
        Set<ApplicationIdentityAttributeEntity> identityAttributes = applicationIdentity.getAttributes();

        /*
         * The non-compounded attribute types have precedence over the members of compounded attribute types.
         */
        Map<AttributeTypeEntity, Boolean> attributeRequirements = new HashMap<AttributeTypeEntity, Boolean>();
        for (ApplicationIdentityAttributeEntity identityAttribute : identityAttributes) {
            LOG.debug("look at non-compounded: " + identityAttribute.getAttributeTypeName());
            AttributeTypeEntity attributeType = identityAttribute.getAttributeType();
            if (attributeType.isCompounded()) {
                continue;
            }
            if (required == false) {
                attributeRequirements.put(attributeType, required == identityAttribute.isRequired()
                        && attributeType.isUserEditable());
            } else {
                attributeRequirements.put(attributeType, required == identityAttribute.isRequired());
            }
        }

        /*
         * Next we go over the compounded attribute types and add their members to the map, using the optionality of the
         * member attribute entity.
         */
        for (ApplicationIdentityAttributeEntity identityAttribute : identityAttributes) {
            LOG.debug("look at compounded: " + identityAttribute.getAttributeTypeName());
            AttributeTypeEntity parentAttributeType = identityAttribute.getAttributeType();
            if (false == parentAttributeType.isCompounded()) {
                continue;
            }
            if (identityAttribute.isRequired() != required) {
                continue;
            }
            for (CompoundedAttributeTypeMemberEntity member : parentAttributeType.getMembers()) {
                LOG.debug("look at compounded member: " + member.getMember().getName());
                AttributeTypeEntity memberAttributeType = member.getMember();
                if (attributeRequirements.containsKey(memberAttributeType)) {
                    /*
                     * If the attribute is already present it's because of a non-compounded attribute type which has
                     * precedence over the member attribute types of a compounded attribute type.
                     */
                    continue;
                }
                if (required == false) {
                    attributeRequirements.put(parentAttributeType, required == identityAttribute.isRequired()
                            && memberAttributeType.isUserEditable());
                } else {
                    attributeRequirements.put(parentAttributeType, required == identityAttribute.isRequired());
                }
            }
        }

        List<AttributeTypeEntity> result = FilterUtil
                .filterToList(attributeRequirements, new AttributeMapEntryFilter());
        return result;
    }


    static class AttributeMapEntryFilter implements MapEntryFilter<AttributeTypeEntity, Boolean> {

        public boolean isAllowed(Entry<AttributeTypeEntity, Boolean> element) {

            LOG.debug("filter out attribute type: " + element.getKey().getName() + " allowed=" + element.getValue());
            return element.getValue();
        }
    }


    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> listOptionalAttributes(@NonEmptyString String applicationName, Locale locale)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException {

        LOG.debug("list optional missing attributes for application: " + applicationName);
        SubjectEntity subject = this.subjectManager.getCallerSubject();

        List<AttributeTypeEntity> optionalApplicationAttributeTypes = getDataAttributeTypes(applicationName, false);

        return listMissingAttributes(subject, optionalApplicationAttributeTypes, locale);
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> listMissingAttributes(@NonEmptyString String applicationName, Locale locale)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException {

        LOG.debug("list missing attributes for application: " + applicationName);
        SubjectEntity subject = this.subjectManager.getCallerSubject();

        List<AttributeTypeEntity> requiredApplicationAttributeTypes = getDataAttributeTypes(applicationName, true);

        return listMissingAttributes(subject, requiredApplicationAttributeTypes, locale);
    }

    /**
     * Returns list of attribute data objects given the subject and the list of attribute types.
     */
    private List<AttributeDO> listMissingAttributes(SubjectEntity subject, List<AttributeTypeEntity> attributeTypes,
            Locale locale) throws PermissionDeniedException, AttributeTypeNotFoundException {

        List<AttributeDO> attributesView = new LinkedList<AttributeDO>();
        for (AttributeTypeEntity attributeType : attributeTypes) {
            LOG.debug("find attribute value for type: " + attributeType.getName());
            Object value = this.proxyAttributeService.findAttributeValue(subject.getUserId(), attributeType.getName());
            if (null == value) {
                addTemplateToView(attributeType, attributesView, locale, true);
            }
        }
        return attributesView;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> listConfirmedIdentity(@NonEmptyString String applicationName, Locale locale)
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        SubjectEntity subject = this.subjectManager.getCallerSubject();
        SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(subject, application);
        Long confirmedIdentityVersion = subscription.getConfirmedIdentityVersion();
        if (null == confirmedIdentityVersion)
            return new LinkedList<AttributeDO>();
        ApplicationIdentityEntity confirmedIdentity = this.applicationIdentityDAO.getApplicationIdentity(application,
                confirmedIdentityVersion);
        Set<ApplicationIdentityAttributeEntity> confirmedAttributeTypes = confirmedIdentity.getAttributes();
        List<AttributeDO> confirmedAttributes = this.attributeTypeDescriptionDecorator
                .addDescriptionFromIdentityAttributes(confirmedAttributeTypes, locale);
        return confirmedAttributes;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> listDeviceAttributes(@NonEmptyString String deviceMappingId,
            @NotNull AttributeTypeEntity attributeType, Locale locale) throws PermissionDeniedException,
            AttributeTypeNotFoundException {

        LOG.debug("list attributes for device: " + deviceMappingId);
        Object value = this.proxyAttributeService.findDeviceAttributeValue(deviceMappingId, attributeType.getName());
        if (null == value)
            return null;

        List<AttributeDO> attributesView = new LinkedList<AttributeDO>();
        addValueToView(value, attributeType, attributesView, locale);
        return attributesView;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void removeAttribute(@NotNull AttributeDO attribute) throws PermissionDeniedException,
            AttributeNotFoundException, AttributeTypeNotFoundException {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        String attributeName = attribute.getName();
        LOG.debug("remove attribute " + attributeName + " for entity with login " + subject);
        LOG.debug("received attribute values: " + attribute);

        AttributeTypeEntity attributeType = getUserRemovableAttributeType(attributeName);

        this.attributeManager.removeAttribute(attributeType, attribute.getIndex(), subject);

        this.historyDAO.addHistoryEntry(subject, HistoryEventType.ATTRIBUTE_REMOVE, Collections.singletonMap(
                SafeOnlineConstants.ATTRIBUTE_PROPERTY, attributeName));
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void addAttribute(@NotNull List<AttributeDO> newAttributeContext) throws PermissionDeniedException,
            AttributeTypeNotFoundException {

        AttributeDO headAttribute = newAttributeContext.get(0);
        String attributeName = headAttribute.getName();
        SubjectEntity subject = this.subjectManager.getCallerSubject();
        LOG.debug("add attribute " + attributeName + " for entity with login " + subject);

        AttributeTypeEntity attributeType = getUserEditableAttributeType(attributeName);

        boolean multivalued = attributeType.isMultivalued();
        if (false == multivalued)
            throw new PermissionDeniedException("attribute type is not multivalued");

        if (newAttributeContext.size() > 1) {
            /*
             * In this case the first entry is the compounded attribute for which the user wants to create a new record.
             */
            if (false == attributeType.isCompounded())
                throw new PermissionDeniedException("attribute type is not compounded");
            AttributeEntity compoundedAttribute = this.attributeDAO.addAttribute(attributeType, subject);
            String compoundedAttributeId = UUID.randomUUID().toString();
            LOG.debug("adding new compounded entry with Id: " + compoundedAttributeId);
            compoundedAttribute.setStringValue(compoundedAttributeId);
            long attributeIndex = compoundedAttribute.getAttributeIndex();
            LOG.debug("compounded attribute index: " + attributeIndex);

            Iterator<AttributeDO> iterator = newAttributeContext.listIterator(1);
            while (iterator.hasNext()) {
                AttributeDO attribute = iterator.next();
                if (false == attribute.isEditable()) {
                    /*
                     * By skipping this entry we allow an easy handling of a compounded attribute record in the GUI.
                     */
                    continue;
                }
                AttributeTypeEntity memberAttributeType = this.attributeTypeDAO.getAttributeType(attribute.getName());
                AttributeEntity memberAttribute = this.attributeDAO.addAttribute(memberAttributeType, subject,
                        attributeIndex);
                LOG.debug("adding member: " + memberAttributeType.getName());
                attribute.copyValueTo(memberAttributeType, memberAttribute);
            }

            this.historyDAO.addHistoryEntry(subject, HistoryEventType.ATTRIBUTE_ADD, Collections.singletonMap(
                    SafeOnlineConstants.ATTRIBUTE_PROPERTY, attributeName));

            return;
        }

        /*
         * Else we're dealing with a regular multi-valued attribute.
         */
        AttributeEntity attribute = this.attributeDAO.addAttribute(attributeType, subject);
        LOG.debug("new attribute index: " + attribute.getAttributeIndex());
        headAttribute.copyValueTo(attributeType, attribute);
        this.historyDAO.addHistoryEntry(subject, HistoryEventType.ATTRIBUTE_ADD, Collections.singletonMap(
                SafeOnlineConstants.ATTRIBUTE_PROPERTY, attributeName));

    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> getAttributeEditContext(@NotNull AttributeDO selectedAttribute)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(selectedAttribute.getName());
        if (attributeType.isCompounded()) {
            List<CompoundedAttributeTypeMemberEntity> members = attributeType.getMembers();
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
                AttributeEntity attribute = this.attributeDAO.findAttribute(subject, memberAttributeType, index);
                AttributeDO memberView = new AttributeDO(memberAttributeType.getName(), memberAttributeType.getType(),
                        true, index, null, null, memberAttributeType.isUserEditable(), false, null, null);
                memberView.setMember(true);
                if (null != attribute) {
                    memberView.setValue(attribute);
                }
                attributeEditContext.add(memberView);
            }

            return attributeEditContext;
        }
        if (attributeType.isCompoundMember())
            throw new IllegalArgumentException("cannot handle members itself.");
        /*
         * Else we're dealing with simple- or multivalued attributes that do not participate in a compounded record
         * somehow.
         */
        List<AttributeDO> attributeEditContext = new LinkedList<AttributeDO>();
        attributeEditContext.add(selectedAttribute);
        return attributeEditContext;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public List<AttributeDO> getAttributeTemplate(@NotNull AttributeDO prototypeAttribute)
            throws AttributeTypeNotFoundException {

        String attributeName = prototypeAttribute.getName();
        LOG.debug("getAttributeTemplate: " + attributeName);
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(prototypeAttribute.getName());

        if (attributeType.isCompounded()) {
            List<AttributeDO> attributeTemplate = new LinkedList<AttributeDO>();

            /*
             * Notice that we mark the entry as single-valued here since we cannot yet pass a usefull attribute index to
             * the GUI.
             */
            AttributeDO compoundedAttribute = new AttributeDO(attributeType.getName(), attributeType.getType(), false,
                    -1, prototypeAttribute.getRawHumanReadableName(), prototypeAttribute.getDescription(),
                    attributeType.isUserEditable(), false, null, null);
            compoundedAttribute.setCompounded(true);
            attributeTemplate.add(compoundedAttribute);

            List<CompoundedAttributeTypeMemberEntity> members = attributeType.getMembers();

            for (CompoundedAttributeTypeMemberEntity member : members) {
                AttributeTypeEntity memberAttributeType = member.getMember();

                /*
                 * Notice that we mark the entry as single-valued here since we cannot yet pass a usefull attribute
                 * index to the GUI.
                 */
                AttributeDO memberAttribute = new AttributeDO(memberAttributeType.getName(), memberAttributeType
                        .getType(), false, -1, null, null, memberAttributeType.isUserEditable(), false, null, null);
                memberAttribute.setMember(true);
                attributeTemplate.add(memberAttribute);
            }

            return attributeTemplate;
        }

        if (attributeType.isCompoundMember())
            throw new IllegalArgumentException("cannot handle compounded members itself");

        /*
         * Notice that we mark the entry as single-valued here since we cannot yet pass a usefull attribute index to the
         * GUI.
         */
        AttributeDO attribute = new AttributeDO(attributeType.getName(), attributeType.getType(), false, -1,
                prototypeAttribute.getRawHumanReadableName(), prototypeAttribute.getDescription(), attributeType
                        .isUserEditable(), false, null, null);
        List<AttributeDO> attributeTemplate = new LinkedList<AttributeDO>();
        attributeTemplate.add(attribute);
        return attributeTemplate;
    }
}
