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
 * bundles.
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

	public Attribute(String name, DatatypeType type, long index,
			String stringValue, Boolean booleanValue) {

		this.name = name;
		this.type = type;
		this.index = index;
		this.stringValue = stringValue;
		this.booleanValue = booleanValue;
	}

	public Attribute(String name, DatatypeType type) {

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

		switch (this.type) {
		case STRING: {
			this.setStringValue((String) value);
			break;
		}
		case BOOLEAN: {
			this.setBooleanValue((Boolean) value);
			break;
		}
		case INTEGER: {
			this.setIntegerValue((Integer) value);
			break;
		}
		case DOUBLE: {
			this.setDoubleValue((Double) value);
			break;
		}
		case DATE: {
			this.setDateValue((Date) value);
			break;
		}
		default: {
			throw new UnsupportedDataTypeException("unsupported data type: "
					+ value.getClass().getName());
		}
		}
	}

	public Object getValue() throws UnsupportedDataTypeException {

		switch (this.type) {
		case STRING:
			return this.getStringValue();
		case BOOLEAN:
			return this.getBooleanValue();
		case INTEGER:
			return this.getIntegerValue();
		case DOUBLE:
			return this.getDoubleValue();
		case DATE:
			return this.getDateValue();
		default:
			throw new UnsupportedDataTypeException("unsupported data type: "
					+ this.type);
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

	@Override
	public Attribute clone() {

		Attribute attribute = new Attribute(this.name, this.type, this.index,
				this.stringValue, this.booleanValue);
		attribute.integerValue = this.integerValue;
		attribute.doubleValue = this.doubleValue;
		attribute.dateValue = this.dateValue;
		return attribute;
	}

	@Override
	public String toString() {

		return "name = " + this.name + " index=" + this.index
				+ " string-value=" + this.stringValue + " integer-value="
				+ this.integerValue;
	}
}
