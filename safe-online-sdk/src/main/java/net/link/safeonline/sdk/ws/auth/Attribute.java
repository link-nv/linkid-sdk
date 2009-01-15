/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.auth;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.ws.common.WebServiceConstants;
import oasis.names.tc.saml._2_0.assertion.AttributeType;


/**
 * <h2>{@link Attribute}<br>
 * <sub>Data-container for OLAS attribute.</sub></h2>
 * 
 * <p>
 * Data-container for a OLAS attribute to be used by the OLAS Authentication Web Service.
 * </p>
 * 
 * <p>
 * <i>Jan 14, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class Attribute {

    private final String          name;

    private final String          friendlyName;

    private final DataType        dataType;

    private final boolean         anonymous;

    private final boolean         optional;

    private final List<Attribute> members;

    private Object                value;


    public Attribute(AttributeType attributeType) {

        this.name = attributeType.getName();
        this.friendlyName = attributeType.getFriendlyName();
        this.dataType = DataType.getDataType(attributeType.getOtherAttributes().get(WebServiceConstants.DATATYPE_ATTRIBUTE));
        this.anonymous = Boolean.valueOf(attributeType.getOtherAttributes().get(WebServiceConstants.DATAMINING_ATTRIBUTE));
        this.optional = Boolean.valueOf(attributeType.getOtherAttributes().get(WebServiceConstants.OPTIONAL_ATTRIBUTE));

        if (null == attributeType.getAttributeValue() || attributeType.getAttributeValue().isEmpty()) {
            this.members = null;
        } else {
            this.members = new LinkedList<Attribute>();
            for (Object memberAttribute : attributeType.getAttributeValue()) {
                AttributeType memberAttributeType = (AttributeType) memberAttribute;
                this.members.add(new Attribute(memberAttributeType));
            }
        }

    }

    /**
     * Returns the attribute's name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Returns the attribute's friendly name, if available. The friendly name is retrieved using the language passed in the initial
     * authentication web service call. If no friendly name is available for that language, the default attribute's name is returned as in
     * {@link #getName()}.
     */
    public String getFriendlyName() {

        return this.friendlyName;
    }

    /**
     * Returns the attribute's data type.
     */
    public DataType getDataType() {

        return this.dataType;
    }

    /**
     * Returns the members of compound attribute.
     */
    public List<Attribute> getMembers() {

        return this.members;
    }

    /**
     * Returns whether this attribute is anonymous or not.
     */
    public boolean isAnonymous() {

        return this.anonymous;
    }

    /**
     * Returns whether this missing attribute is optional or not.
     */
    public boolean isOptional() {

        return this.optional;
    }

    /**
     * Sets the value of this attribute. Throws a {@link RuntimeException} if the type of value does not correspond to the expected data
     * type {@link #getDataType()}.
     */
    public void setValue(Object value) {

        if (this.dataType == DataType.COMPOUNDED)
            throw new RuntimeException("Compound attributes do not have a value.");

        if (this.dataType == DataType.STRING && !(value instanceof String) || this.dataType == DataType.BOOLEAN
                && !(value instanceof Boolean) || this.dataType == DataType.DATE && !(value instanceof Date)
                || this.dataType == DataType.DOUBLE && !(value instanceof Double) || this.dataType == DataType.INTEGER
                && !(value instanceof Integer))
            throw new RuntimeException("Invalid value for datatype " + this.dataType.toString());

        this.value = value;
    }

    public Object getValue() {

        return this.value;
    }

    public AttributeType getAttributeType() {

        AttributeType attributeType = new AttributeType();
        attributeType.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
        attributeType.setName(this.name);
        attributeType.setFriendlyName(this.friendlyName);
        if (this.dataType == DataType.COMPOUNDED) {
            for (Attribute memberAttribute : this.members) {
                attributeType.getAttributeValue().add(memberAttribute.getAttributeType());
            }
        } else {
            attributeType.getAttributeValue().add(this.value);
        }
        attributeType.getOtherAttributes().put(WebServiceConstants.DATATYPE_ATTRIBUTE, this.dataType.getValue());
        return attributeType;

    }

}
