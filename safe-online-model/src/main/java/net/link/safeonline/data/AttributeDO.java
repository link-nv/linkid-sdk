/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBException;

import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Attribute Data Object. Used to transfer data between service and user
 * application. This has been done to make life in the presentation layer
 * easier.
 * 
 * @author fcorneli
 * 
 */
public class AttributeDO implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private String name;

	private DatatypeType type;

	private long index;

	private String humanReadableName;

	private String description;

	private boolean editable;

	private boolean dataMining;

	private String stringValue;

	private Boolean booleanValue;

	private Integer integerValue;

	private Double doubleValue;

	private Date dateValue;

	private boolean multivalued;

	private boolean compounded;

	private boolean member;

	private boolean required;

	private boolean userVisible;

	public AttributeDO(String name, DatatypeType type, boolean multivalued,
			long index, String humanReadableName, String description,
			boolean editable, boolean dataMining, String stringValue,
			Boolean booleanValue) {
		this.name = name;
		this.type = type;
		this.multivalued = multivalued;
		this.index = index;
		this.humanReadableName = humanReadableName;
		this.description = description;
		this.editable = editable;
		this.dataMining = dataMining;
		this.stringValue = stringValue;
		this.booleanValue = booleanValue;
	}

	public AttributeDO(String name, DatatypeType type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Gives back the URN name of the attribute type.
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStringValue() {
		return this.stringValue;
	}

	public void setStringValue(String value) {
		this.stringValue = value;
	}

	public boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gives back the human readable name of the attribute type corresponding
	 * with this attribute. If the i18n name is <code>null</code> this method
	 * gives back the URN machine name. In case this attribute value is part of
	 * a multi-valued attribute we also append the attribute index to the human
	 * readable name. We increase the index by one since human beings tend to
	 * start counting from 1.
	 * 
	 */
	public String getHumanReadableName() {
		String viewName;
		if (null != this.humanReadableName) {
			viewName = this.humanReadableName;
		} else {
			viewName = this.name;
		}
		if (true == this.multivalued) {
			viewName += " " + (this.index + 1);
		}
		return viewName;
	}

	public String getRawHumanReadableName() {
		String viewName;
		if (null != this.humanReadableName) {
			viewName = this.humanReadableName;
		} else {
			viewName = this.name;
		}
		return viewName;
	}

	public void setHumanReadableName(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}

	public boolean isDataMining() {
		return this.dataMining;
	}

	public void setDataMining(boolean dataMining) {
		this.dataMining = dataMining;
	}

	/**
	 * Gets the boolean value. Can be <code>null</code>.
	 * 
	 */
	public Boolean getBooleanValue() {
		return this.booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public Date getDateValue() {
		return this.dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public Double getDoubleValue() {
		return this.doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Integer getIntegerValue() {
		return this.integerValue;
	}

	public void setIntegerValue(Integer integerValue) {
		this.integerValue = integerValue;
	}

	/**
	 * Gets the value. The {@link #getValue()} and
	 * {@link #setValue(AttributeDO)} methods are used by the presentation layer
	 * to allow for easy Expression Language expressions in the JSF pages.
	 * 
	 */
	public AttributeDO getValue() {
		return this;
	}

	/**
	 * Sets the value. Here we do a deep-copy of the values.
	 * 
	 * @param value
	 */
	public void setValue(AttributeDO value) {
		this.booleanValue = value.booleanValue;
		this.stringValue = value.stringValue;
		this.integerValue = value.integerValue;
		this.doubleValue = value.doubleValue;
		this.dateValue = value.dateValue;
	}

	public void setValue(AttributeEntity attribute) {
		AttributeTypeEntity attributeType = attribute.getAttributeType();
		DatatypeType datatypeType = attributeType.getType();
		switch (datatypeType) {
		case STRING:
		case LOGIN:
			this.setStringValue(attribute.getStringValue());
			break;
		case BOOLEAN:
			this.setBooleanValue(attribute.getBooleanValue());
			break;
		case INTEGER:
			this.setIntegerValue(attribute.getIntegerValue());
			break;
		case DOUBLE:
			this.setDoubleValue(attribute.getDoubleValue());
			break;
		case DATE:
			this.setDateValue(attribute.getDateValue());
			break;
		default:
			throw new EJBException("unsupported data type: " + datatypeType);
		}
	}

	public void setValue(Object value) {
		if (value.getClass().equals(String.class))
			this.setStringValue((String) value);
		else if (value.getClass().equals(Boolean.class))
			this.setBooleanValue((Boolean) value);
		else if (value.getClass().equals(Integer.class))
			this.setIntegerValue((Integer) value);
		else if (value.getClass().equals(Double.class))
			this.setDoubleValue((Double) value);
		else if (value.getClass().equals(Date.class))
			this.setDateValue((Date) value);
		else
			throw new EJBException("unsupported data type: "
					+ value.getClass().getName());

	}

	public DatatypeType getType() {
		return this.type;
	}

	public void setType(DatatypeType type) {
		this.type = type;
	}

	/**
	 * Gives back the index of this attribute. This only really makes sense in
	 * the event of multi-valued attributes. For single-valued attributes the
	 * index defaults to zero.
	 * 
	 */
	public long getIndex() {
		return this.index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	/**
	 * Marks whether this attribute value is part of a multi-valued attribute or
	 * not.
	 * 
	 */
	public boolean isMultivalued() {
		return this.multivalued;
	}

	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}

	/**
	 * Marks that this attribute entry is the title entry of a compounded
	 * attribute record. This flag will be used for visualization.
	 * 
	 */
	public boolean isCompounded() {
		return this.compounded;
	}

	public void setCompounded(boolean compounded) {
		this.compounded = compounded;
	}

	/**
	 * Marks that this attribute entry is a member entry of a compounded
	 * attribute record. This flag will be used for visualization.
	 * 
	 */
	public boolean isMember() {
		return this.member;
	}

	public void setMember(boolean member) {
		this.member = member;
	}

	/**
	 * Marks whether this attribute is required or not. For compounded member
	 * attribute the value could be optional.
	 * 
	 */
	public boolean isRequired() {
		return this.required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Marks whether this attribute is user visible or not.
	 * 
	 */
	public boolean isUserVisible() {
		return this.userVisible;
	}

	public void setUserVisible(boolean userVisible) {
		this.userVisible = userVisible;
	}

	@Override
	public AttributeDO clone() {
		AttributeDO attribute = new AttributeDO(this.name, this.type,
				this.multivalued, this.index, this.humanReadableName,
				this.description, this.editable, this.dataMining,
				this.stringValue, this.booleanValue);
		attribute.integerValue = this.integerValue;
		attribute.doubleValue = this.doubleValue;
		attribute.dateValue = this.dateValue;
		return attribute;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", this.name).append("multi-valued",
						this.multivalued).append("index", this.index).append(
						"string-value", this.stringValue).append(
						"integer-value", this.integerValue).toString();
	}

	/**
	 * Copies the value of this attribute data object to the (attached) target
	 * attribute entity according to the datatype constraints by the given
	 * attribute type.
	 * 
	 * @param attributeType
	 * @param targetAttribute
	 */
	public void copyValueTo(AttributeTypeEntity attributeType,
			AttributeEntity targetAttribute) {
		switch (attributeType.getType()) {
		case STRING:
		case LOGIN:
			targetAttribute.setStringValue(this.stringValue);
			break;
		case BOOLEAN:
			targetAttribute.setBooleanValue(this.booleanValue);
			break;
		case INTEGER:
			targetAttribute.setIntegerValue(this.integerValue);
			break;
		case DOUBLE:
			targetAttribute.setDoubleValue(this.doubleValue);
			break;
		case DATE:
			targetAttribute.setDateValue(this.dateValue);
			break;
		default:
			throw new EJBException("datatype not supported: " + this.type);
		}
	}

}
