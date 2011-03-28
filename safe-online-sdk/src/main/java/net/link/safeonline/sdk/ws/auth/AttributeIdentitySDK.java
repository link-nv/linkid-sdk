/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.auth;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.attribute.provider.AttributeType;
import net.link.safeonline.attribute.provider.Compound;
import net.link.safeonline.attribute.provider.DataType;
import net.link.safeonline.sdk.ws.WebServiceConstants;


/**
 * <h2>{@link AttributeIdentitySDK}<br> <sub>Data-container for linkID attribute.</sub></h2>
 *
 * <p> Data-container for a linkID attribute to be used by the linkID Authentication Web Service. </p>
 *
 * <p> <i>Jan 14, 2009</i> </p>
 *
 * @author wvdhaute
 */
public class AttributeIdentitySDK extends AttributeSDK<Serializable> {

    private final AttributeType attributeType;

    // identity info
    private final String       friendlyName;
    private final String       groupName;
    private final boolean      anonymous;
    private final boolean      optional;
    private final boolean      confirmationNeeded;
    private       Confirmation confirmation;


    public enum Confirmation {

        CONFIRMED,
        REJECTED,
        NONE
    }

    public AttributeIdentitySDK(oasis.names.tc.saml._2_0.assertion.AttributeType attributeType) {

        super( attributeType.getOtherAttributes().get( WebServiceConstants.ATTRIBUTE_ID ), attributeType.getName() );
        this.attributeType = new AttributeType( attributeType.getName(),
                DataType.getDataType( attributeType.getOtherAttributes().get( WebServiceConstants.DATATYPE_ATTRIBUTE ) ),
                Boolean.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.MULTIVALUED_ATTRIBUTE ) ) );

        friendlyName = attributeType.getFriendlyName();
        groupName = attributeType.getOtherAttributes().get( WebServiceConstants.GROUP_NAME_ATTRIBUTE );
        anonymous = Boolean.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.DATAMINING_ATTRIBUTE ) );
        optional = Boolean.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.OPTIONAL_ATTRIBUTE ) );
        confirmationNeeded = Boolean.valueOf(
                attributeType.getOtherAttributes().get( WebServiceConstants.CONFIRMATION_REQUIRED_ATTRIBUTE ) );
        confirmation = Confirmation.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.CONFIRMED_ATTRIBUTE ) );

        if (null != attributeType.getAttributeValue()) {
            if (this.attributeType.isCompound()) {
                List<AttributeIdentitySDK> members = new LinkedList<AttributeIdentitySDK>();
                for (Object memberAttribute : attributeType.getAttributeValue()) {
                    oasis.names.tc.saml._2_0.assertion.AttributeType memberAttributeType = (oasis.names.tc.saml._2_0.assertion.AttributeType) memberAttribute;
                    members.add( new AttributeIdentitySDK( memberAttributeType ) );
                }
                setValue( new Compound( members ) );
            } else {
                setValue( (Serializable) attributeType.getAttributeValue().get( 0 ) );
            }
        }
    }

    public AttributeType getAttributeType() {

        return attributeType;
    }

    /**
     * @return the attribute's friendly name, if available. The friendly name is retrieved using the language passed in the initial
     *         authentication web service call. If no friendly name is available for that language, the default attribute's name is returned
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

    /**
     * @return whether this attribute is confirmed/rejected or neither.
     */
    public Confirmation getConfirmation() {

        return confirmation;
    }

    /**
     * Set confirmation status
     *
     * @param confirmation confirmation
     */
    public void setConfirmation(Confirmation confirmation) {

        this.confirmation = confirmation;
    }

    public oasis.names.tc.saml._2_0.assertion.AttributeType toSDK() {

        oasis.names.tc.saml._2_0.assertion.AttributeType attributeType = new oasis.names.tc.saml._2_0.assertion.AttributeType();
        attributeType.setNameFormat( WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC );
        attributeType.setName( attributeType.getName() );
        attributeType.setFriendlyName( friendlyName );

        if (this.attributeType.isCompound()) {
            for (AttributeSDK<?> memberSDK : ((Compound) getValue()).getMembers()) {
                AttributeIdentitySDK member = (AttributeIdentitySDK) memberSDK;
                attributeType.getAttributeValue().add( member.toSDK() );
            }
        } else {
            attributeType.getAttributeValue().add( getValue() );
        }
        attributeType.getOtherAttributes().put( WebServiceConstants.DATATYPE_ATTRIBUTE, this.attributeType.getType().getValue() );
        attributeType.getOtherAttributes()
                .put( WebServiceConstants.MULTIVALUED_ATTRIBUTE, Boolean.toString( this.attributeType.isMultivalued() ) );
        attributeType.getOtherAttributes().put( WebServiceConstants.DATAMINING_ATTRIBUTE, Boolean.toString( anonymous ) );
        attributeType.getOtherAttributes().put( WebServiceConstants.OPTIONAL_ATTRIBUTE, Boolean.toString( optional ) );
        attributeType.getOtherAttributes()
                .put( WebServiceConstants.CONFIRMATION_REQUIRED_ATTRIBUTE, Boolean.toString( confirmationNeeded ) );
        attributeType.getOtherAttributes().put( WebServiceConstants.CONFIRMED_ATTRIBUTE, confirmation.name() );
        attributeType.getOtherAttributes().put( WebServiceConstants.ATTRIBUTE_ID, getId() );
        attributeType.getOtherAttributes().put( WebServiceConstants.GROUP_NAME_ATTRIBUTE, groupName );
        return attributeType;
    }
}
