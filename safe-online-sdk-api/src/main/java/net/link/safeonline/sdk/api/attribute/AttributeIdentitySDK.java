/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.api.attribute;

import java.io.Serializable;


/**
 * <h2>{@link AttributeIdentitySDK}<br> <sub>Data-container for linkID attribute.</sub></h2>
 * <p/>
 * <p> Data-container for a linkID attribute to be used by the linkID Authentication Web Service. </p>
 * <p/>
 * <p> <i>Jan 14, 2009</i> </p>
 *
 * @author wvdhaute
 */
public class AttributeIdentitySDK extends AttributeSDK<Serializable> {

    private final AttributeType attributeType;

    // identity info
    private final String  friendlyName;
    private final String  groupName;
    private final boolean anonymous;
    private final boolean optional;
    private final boolean confirmationNeeded;
    private       boolean confirmed;

    public AttributeIdentitySDK(final String id, final AttributeType attributeType, final String friendlyName, final String groupName,
                                final boolean anonymous, final boolean optional, final boolean confirmationNeeded, final boolean confirmed,
                                final Serializable value) {

        super( id, attributeType.getName(), null );

        this.attributeType = attributeType;
        this.friendlyName = friendlyName;
        this.groupName = groupName;
        this.anonymous = anonymous;
        this.optional = optional;
        this.confirmationNeeded = confirmationNeeded;
        this.confirmed = confirmed;
        setValue( value );
    }

    public AttributeType getAttributeType() {

        return attributeType;
    }

    /**
     * @return the attribute's friendly name, if available. The friendly name is retrieved using the language passed in the initial
     *         authentication web service call. If no friendly name is available for that language, the default attribute's name is
     *         returned
     *         as in {@link #getName()}.
     */
    public String getFriendlyName() {

        return friendlyName;
    }

    /**
     * @return the group name of this attribute. Attributes are sometimes grouped together for visualisation purposes.
     */
    public String getGroupName() {

        return groupName;
    }

    /**
     * @return whether this attribute is anonymous or not.
     */
    public boolean isAnonymous() {

        return anonymous;
    }

    /**
     * @return whether this missing attribute is optional or not.
     */
    public boolean isOptional() {

        return optional;
    }

    /**
     * @return whether confirmation is needed for this attribute. This makes sense in case a member of a compound is specified in an
     *         application's identity and the parent is not. In this case the complete compound attribute and its members will still be
     *         added to be able to add/edit the attribute.
     */
    public boolean isConfirmationNeeded() {

        return confirmationNeeded;
    }

    public boolean isConfirmed() {

        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {

        this.confirmed = confirmed;
    }
}
