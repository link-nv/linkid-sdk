/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.io.Serializable;

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

	private String type;

	private long index;

	private String humanReadableName;

	private String description;

	private boolean editable;

	private boolean dataMining;

	private String stringValue;

	private Boolean booleanValue;

	private boolean multivalued;

	public AttributeDO(String name, String type, boolean multivalued,
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

	public AttributeDO(String name, String type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Gives back the URN name of the attribute type.
	 * 
	 * @return
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
	 * @return
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

	public void setHumanReadableName(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}

	public boolean isDataMining() {
		return dataMining;
	}

	public void setDataMining(boolean dataMining) {
		this.dataMining = dataMining;
	}

	/**
	 * Gets the boolean value. Can be <code>null</code>.
	 * 
	 * @return
	 */
	public Boolean getBooleanValue() {
		return this.booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	/**
	 * Gets the value. The {@link #getValue()} and
	 * {@link #setValue(AttributeDO)} methods are used by the presentation layer
	 * to allow for easy Expression Language expressions in the JSF pages.
	 * 
	 * @return
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
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gives back the index of this attribute. This only really makes sense in
	 * the event of multi-valued attributes. For single-valued attributes the
	 * index defaults to zero.
	 * 
	 * @return
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
	 * @return
	 */
	public boolean isMultivalued() {
		return this.multivalued;
	}

	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}

	@Override
	public AttributeDO clone() {
		AttributeDO attribute = new AttributeDO(this.name, this.type,
				this.multivalued, this.index, this.humanReadableName,
				this.description, this.editable, this.dataMining,
				this.stringValue, this.booleanValue);
		return attribute;
	}
}
