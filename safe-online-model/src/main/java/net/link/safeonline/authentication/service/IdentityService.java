/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;
import java.util.Locale;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Interface of service component to access the identity data of a caller subject.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface IdentityService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "IdentityServiceBean/local";


    /**
     * Gives back the authentication history of the user linked to the caller principal.
     * 
     * @return a list of history entries.
     */
    List<HistoryEntity> listHistory();

    /**
     * Gives back the authentication history of the specified user.
     * 
     * @param subject
     * @return a list of history entries.
     */
    List<HistoryEntity> listHistory(SubjectEntity subject);

    /**
     * Saves an (new) attribute value for the current user.
     * 
     * @throws PermissionDeniedException
     *             if the user is not allowed to edit the attribute.
     * @throws AttributeTypeNotFoundException
     */
    void saveAttribute(AttributeDO attribute)
            throws PermissionDeniedException, AttributeTypeNotFoundException;

    /**
     * Gives back a list of attributes for the current user. Only the attributes that are user visible will be returned.
     * 
     * @param locale
     *            the optional locale that should be used to i18n the response.
     * 
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     * @throws ApplicationIdentityNotFoundException
     */
    List<AttributeDO> listAttributes(Locale locale)
            throws AttributeTypeNotFoundException, PermissionDeniedException, ApplicationIdentityNotFoundException;

    /**
     * Gives back a list of all attribute for the specified user. Also attributes marked as not visible will be returned.
     * 
     * @param subject
     * @param locale
     *            the optional locale that should be used to i18n the response
     * 
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     */
    List<AttributeDO> listAttributes(SubjectEntity subject, Locale locale)
            throws PermissionDeniedException, AttributeTypeNotFoundException;

    /**
     * Gives back a list of all attributes of the specified type for the specified user.
     * 
     * @param subject
     * @param attributeType
     * @param locale
     *            the optional locale that should be used to i18n the response
     * 
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     * @throws SubjectNotFoundException
     * 
     */
    List<AttributeDO> listAttributes(SubjectEntity subject, AttributeTypeEntity attributeType, Locale locale)
            throws PermissionDeniedException, AttributeTypeNotFoundException, SubjectNotFoundException;

    /**
     * Checks whether confirmation is required over the usage of the identity attributes use by the given application.
     * 
     * @param applicationName
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     * @throws ApplicationIdentityNotFoundException
     */
    boolean isConfirmationRequired(String applicationName)
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException;

    /**
     * Confirm the current identity for the given application.
     * 
     * TODO: add version to be confirmed.
     * 
     * To make this method really bullet proof we would have to pass the version number itself. This because it's possible that the operator
     * is changing the identity while the user is confirming it. This would make the user to confirm a more recent identity version that the
     * one he was presented.
     * 
     * @param applicationName
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     * @throws ApplicationIdentityNotFoundException
     */
    void confirmIdentity(String applicationName)
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException;

    /**
     * Lists the attributes for which the user has confirmed an identity on the given application.
     * 
     * @param applicationName
     * @param locale
     *            the optional locale.
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     * @throws ApplicationIdentityNotFoundException
     */
    List<AttributeDO> listConfirmedIdentity(String applicationName, Locale locale)
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException;

    /**
     * Gives back a list of identity attributes that need to be confirmed by this user in order to be in-line with the latest identity
     * requirement of the given application.
     * 
     * @param applicationName
     * @param locale
     *            the optional locale to be applied to the result.
     * @throws ApplicationNotFoundException
     * @throws ApplicationIdentityNotFoundException
     * @throws SubscriptionNotFoundException
     */
    List<AttributeDO> listIdentityAttributesToConfirm(String applicationName, Locale locale)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, SubscriptionNotFoundException;

    /**
     * Checks whether the current user still needs to fill in some attribute values for being able to use the given application.
     * 
     * @param applicationName
     * @return <code>true</code> if there are missing attributes, <code>false</code> otherwise.
     * @throws ApplicationNotFoundException
     * @throws ApplicationIdentityNotFoundException
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     */
    boolean hasMissingAttributes(String applicationName)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException;

    /**
     * Gives back a list of the user's missing attributes for the given application. This method also returns a list of {@link AttributeDO}
     * objects to make life easier in the view/control. The control components will most likely afterwards call
     * {@link #saveAttribute(AttributeDO)} to save new values for the missing attributes.
     * 
     * @param applicationName
     * @param locale
     *            the optional locale for i18n of the result.
     * @throws ApplicationNotFoundException
     * @throws ApplicationIdentityNotFoundException
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     */
    List<AttributeDO> listMissingAttributes(String applicationName, Locale locale)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException;

    /**
     * Gives back a list of the user's optional attributes for the given application.This method also returns a list of {@link AttributeDO}
     * objects to make life easier in the view/control. The control components will most likely afterwards call
     * {@link #saveAttribute(AttributeDO)} to save new values for the optional attributes.
     * 
     * @param application
     * @param locale
     * @return
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     * @throws ApplicationIdentityNotFoundException
     * @throws ApplicationNotFoundException
     */
    List<AttributeDO> listOptionalAttributes(String application, Locale local)
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException;

    /**
     * Removes an attribute. A user can only remove editable attributes. In case this attribute is part of a multivalued attribute set we
     * will reorder the remaining attributes in order to have a consistent perceived sequencing. In case of a compounded multi-valued
     * attribute a resequencing of all member attributes takes place.
     * 
     * @param attribute
     * @throws PermissionDeniedException
     * @throws AttributeNotFoundException
     * @throws AttributeTypeNotFoundException
     */
    void removeAttribute(AttributeDO attribute)
            throws PermissionDeniedException, AttributeNotFoundException, AttributeTypeNotFoundException;

    /**
     * Adds an attribute.
     * 
     * <p>
     * This method only really makes sense for multi-valued attributes since a user will never create non-existing attributes just for fun.
     * A user is only supposed to edit existing attribute. And if the attribute is multi-valued, then editing includes creation. This also
     * implies that the attibute type must be marked as user editable.
     * </p>
     * 
     * <p>
     * In case the user wants to add a compounded multi-valued attribute the input list will contain more than one attribute data object.
     * The first entry holds the compounded attribute type for which the user wishes to create a new record. Followed by an entry for each
     * member attribute of the compounded attribute. The method signature has been optimized for ease of usage by the user web application.
     * </p>
     * 
     * @param newAttributeContext
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     */
    void addAttribute(List<AttributeDO> newAttributeContext)
            throws PermissionDeniedException, AttributeTypeNotFoundException;

    /**
     * This method simply returns a set of attributes that the user can edit when he previously selected the selectedAttribute for editing.
     * This method allows editing of compounded attributes. In case of compounded attributes the appropriate set of member attributes will
     * be returned.
     * 
     * @param selectedAttribute
     * @throws AttributeTypeNotFoundException
     */
    List<AttributeDO> getAttributeEditContext(AttributeDO selectedAttribute)
            throws AttributeTypeNotFoundException;

    /**
     * Creates a template that can be used to create a new attribute according to the attribute type of the given prototype attribute.
     * 
     * @param prototypeAttribute
     * @throws AttributeTypeNotFoundException
     */
    List<AttributeDO> getAttributeTemplate(AttributeDO prototypeAttribute)
            throws AttributeTypeNotFoundException;
}
