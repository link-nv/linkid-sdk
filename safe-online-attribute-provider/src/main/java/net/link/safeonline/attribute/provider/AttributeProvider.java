/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import net.link.safeonline.attribute.provider.confirmation.AttributeConfirmationPanel;
import net.link.safeonline.attribute.provider.exception.*;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.input.DefaultAttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;
import net.link.util.j2ee.JNDIUtils;
import net.link.util.wicket.component.WicketPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jetbrains.annotations.Nullable;

public abstract class AttributeProvider implements Serializable {

    public static final String ATTRIBUTE_PROVIDER_JNDI_CONTEXT = "SafeOnline/attributes";
    public static final String ATTRIBUTE_PROVIDER_JNDI_PREFIX  = ATTRIBUTE_PROVIDER_JNDI_CONTEXT + '/';

    /**
     * @param linkIDService   LinkID services available to the implementation.
     * @param userId          userId to return attributes from
     * @param attributeName   attribute to return values for
     * @param filterInvisible filter userInvisble member attributes of compounds.
     *
     * @return all {@link AttributeCore}'s for specified user and attribute name.
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     */
    public abstract List<AttributeCore> listAttributes(LinkIDService linkIDService, String userId, String attributeName,
                                                       boolean filterInvisible)
            throws AttributeProviderRuntimeException;

    /**
     * Fetch attribute for specified user and attribute ID.
     *
     * @param linkIDService LinkID services available to the implementation.
     * @param userId        userId to find attribute for
     * @param attributeName attribute type of attribute to find
     * @param attributeId   attribute ID of attribute to find.
     *
     * @return {@link AttributeCore} or {@code null} if not found.
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     */
    @Nullable
    public abstract AttributeCore findAttribute(LinkIDService linkIDService, String userId, String attributeName, String attributeId)
            throws AttributeProviderRuntimeException;

    /**
     * Fetch compound attribute of specified type which has a member of specified type with specified value
     *
     * @param linkIDService       LinkID services available to the implementation.
     * @param userId              userId to find attribute for
     * @param parentAttributeName attribute type of the parent
     * @param memberAttributeName attribute type of the member
     * @param memberValue         value of the member attribute
     *
     * @return {@link AttributeCore} or {@code null} if not found.
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     */
    @Nullable
    public abstract AttributeCore findCompoundAttributeWhere(LinkIDService linkIDService, String userId, String parentAttributeName,
                                                             String memberAttributeName, Serializable memberValue)
            throws AttributeProviderRuntimeException;

    /**
     * Removes an attribute for the specified subject.
     *
     * @param linkIDService LinkID services available to the implementation.
     * @param userId        userId to remove the attributes from
     * @param attributeName attribute type of values to be removed
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     */
    public abstract void removeAttributes(LinkIDService linkIDService, String userId, String attributeName)
            throws AttributeProviderRuntimeException;

    /**
     * Removes an attribute for the specified subject.
     *
     * @param linkIDService LinkID services available to the implementation.
     * @param userId        userId to remove the attribute from
     * @param attributeName attribute type of value to be removed
     * @param attributeId   attributeId of value to be removed
     *
     * @throws AttributeNotFoundException no value found.
     * @throws AttributeProviderRuntimeException
     *                                    something went unexpectely wrong
     */
    public abstract void removeAttribute(LinkIDService linkIDService, String userId, String attributeName, String attributeId)
            throws AttributeProviderRuntimeException, AttributeNotFoundException;

    /**
     * Remove all attributes with specified attribute name.
     *
     * @param linkIDService LinkID services available to the implementation.
     * @param attributeName attribute type of attributes to remove.
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     */
    public abstract void removeAttributes(LinkIDService linkIDService, String attributeName)
            throws AttributeProviderRuntimeException;

    /**
     * Create/modify the specified {@link AttributeCore} for specified user.
     *
     *
     * @param linkIDService LinkID services available to the implementation.
     * @param userId        userId to set attribute for.
     * @param attribute     attribute to set for subject.
     *
     * @return the updated/created attribute.
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     * @throws AttributePermissionDeniedException
     *          the user is not allowed to modify the attribute
     */
    public abstract AttributeCore setAttribute(LinkIDService linkIDService, String userId, AttributeCore attribute)
            throws AttributeProviderRuntimeException, AttributePermissionDeniedException;

    /**
     * Confirm (e.g. confirm an email address) the specified attribute with specified confirmationId.
     *
     * @param linkIDService
     * @param userId
     * @param attributeConfirmationId
     * @param attribute
     */
    public void confirmAttribute(LinkIDService linkIDService, String userId, String attributeConfirmationId, AttributeCore attribute)
            throws AttributePermissionDeniedException {

    }

    /**
     * Returns the user id associated with a particular attribute confirmations session id
     * Returns null if there is no user for the confirmationId
     *
     * @param confirmationId
     * @return
     */
    public String getUserIdForConfirmationId(String confirmationId){
        return null;
    }

    /**
     * These {@link AttributeType}'s will be registered into LinkID if not yet so.
     *
     * @return {@link List} of supported {@link AttributeType}'s by this provider.
     */
    public abstract List<AttributeType> getSupportedAttributeTypes();

    /**
     * @param linkIDService LinkID services available to the implementation.
     * @param subjects      list of userIds to query for
     * @param attributeName name of the attribute
     *
     * @return map containing a list of unique values of an attribute with a count of how many times these values occur
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     */
    public abstract Map<Serializable, Long> categorize(LinkIDService linkIDService, List<String> subjects, String attributeName)
            throws AttributeProviderRuntimeException;

    /**
     * Callback for initialization of e.g. some configuration for the implementation.
     *
     * @param linkIDService LinkID services available to the implementation.
     */
    public abstract void intialize(LinkIDService linkIDService);

    public abstract AttributeProvider getAttributeProvider();

    public abstract String getName();

    /**
     * @return the JNDI location where the implementation will be bound.
     */
    public String getJndiLocation() {

        return String.format( "%s%s", ATTRIBUTE_PROVIDER_JNDI_PREFIX, getName() );
    }

    protected void register() {

        try {
            JNDIUtils.bindComponent( getJndiLocation(), getAttributeProvider() );
        }
        catch (NamingException e) {
            throw new RuntimeException( String.format( "Unable to bind Attribute provider \"%s\" to \"%s\"", getName(), getJndiLocation() ),
                    e );
        }
    }

    protected void unregister() {

        try {
            JNDIUtils.unbindComponent( getJndiLocation() );
        }
        catch (NamingException e) {
            throw new RuntimeException(
                    String.format( "Unable to unbind Attribute provider \"%s\" to \"%s\"", getName(), getJndiLocation() ), e );
        }
    }

    /**
     * Override this method if you want to provide a non-default attribute input panel.
     *
     * @param linkIDService linkID services provided to attribute provider implementations.
     * @param id            wicket id of panel
     * @param userId        the user ID
     * @param attribute     attribute to get panel for
     *
     * @return a customized {@link WicketPanel} for the specified {@link AttributeCore}.
     *
     * @throws AttributeProviderRuntimeException
     *          something went unexpectely wrong
     */
    public AttributeInputPanel getAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                      final AttributeCore attribute)
            throws AttributeProviderRuntimeException {

        return getDefaultAttributeInputPanel( id, attribute );
    }

    protected static AttributeInputPanel getDefaultAttributeInputPanel(String id, final AttributeCore attribute) {

        return new DefaultAttributeInputPanel( id, attribute );
    }

    /**
     * Returns a confirmation panel for the given attribute and confirmation Id, or 'null' if there is none
     * @param linkIDService
     * @param id
     * @param userId
     * @param attribute
     * @param confirmationId
     * @return
     */
    public AttributeConfirmationPanel getAttributeConfirmationPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                       final AttributeCore attribute, final String confirmationId){

        return null;
    }
}
