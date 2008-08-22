/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.osgi.plugin;

import java.io.Serializable;
import java.util.Date;

import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;

/**
 * <h2>{@link Attribute}<br>
 * <sub>Attibute Data Object.</sub></h2>
 * 
 * <p>
 * Attribute Data Object. Used to transfer data between OLAS and OSGI plugin
 * bundles. OLAS Attribute Service API.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class Attribute implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private String name;

	private DatatypeType type;

	private long index;

	private String stringValue;

	private Boolean booleanValue;

	private Integer integerValue;

	private Double doubleValue;

	private Date dateValue;

	private boolean multivalued;

	private boolean compounded;

	private boolean member;

	public Attribute(String name, DatatypeType type, boolean multivalued,
			long index, String stringValue, Boolean booleanValue) {

		this.name = name;
		this.type = type;
		this.multivalued = multivalued;
		this.index = index;
		this.stringValue = stringValue;
		this.booleanValue = booleanValue;

		setCompounded();
	}

	public Attribute(String name, DatatypeType type) {

		this.name = name;
		this.type = type;

		setCompounded();
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

	public void setValue(Object value) throws UnsupportedDataTypeException {

		if (value.getClass().equals(String.class)) {
			this.setStringValue((String) value);
		} else if (value.getClass().equals(Boolean.class)) {
			this.setBooleanValue((Boolean) value);
		} else if (value.getClass().equals(Integer.class)) {
			this.setIntegerValue((Integer) value);
		} else if (value.getClass().equals(Double.class)) {
			this.setDoubleValue((Double) value);
		} else if (value.getClass().equals(Date.class)) {
			this.setDateValue((Date) value);
		} else {
			throw new UnsupportedDataTypeException("unsupported data type: "
					+ value.getClass().getName());
		}

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

	private void setCompounded() {

		if (null == this.type) {
			return;
		} else if (this.type.equals(DatatypeType.COMPOUNDED)) {
			this.compounded = true;
		} else {
			this.compounded = false;
		}
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

	@Override
	public Attribute clone() {

		Attribute attribute = new Attribute(this.name, this.type,
				this.multivalued, this.index, this.stringValue,
				this.booleanValue);
		attribute.integerValue = this.integerValue;
		attribute.doubleValue = this.doubleValue;
		attribute.dateValue = this.dateValue;
		return attribute;
	}

	@Override
	public String toString() {

		return "name = " + this.name + " multi-valued= " + this.multivalued
				+ " index=" + this.index + " string-value=" + this.stringValue
				+ " integer-value=" + this.integerValue;
	}
}
