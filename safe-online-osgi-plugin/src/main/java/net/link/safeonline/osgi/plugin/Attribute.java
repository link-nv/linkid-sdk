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
 * Attribute Data Object. Used to transfer data between OLAS and the OSGI plugin
 * bundles. The {{@link #index} field is used in case of multivalued or
 * compound attributes.
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

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            attribute name
	 * @param type
	 *            attribute data type
	 * @param index
	 *            index used by multivalued attributes, counting starts from 0.
	 */
	public Attribute(String name, DatatypeType type, long index) {

		this.name = name;
		this.type = type;
		this.index = index;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            attribute name
	 * @param type
	 *            attribute data type
	 */
	public Attribute(String name, DatatypeType type) {

		this.name = name;
		this.type = type;
	}

	/**
	 * Gives back the URN name of the attribute.
	 * 
	 */
	public String getName() {

		return this.name;
	}

	/**
	 * Sets the URN name of the attribute
	 * 
	 * @param name
	 */
	public void setName(String name) {

		this.name = name;
	}

	/**
	 * Returns the string value. Can be <code>null</code> if the {{@link #type}
	 * is different
	 * 
	 * @return string value
	 */
	public String getStringValue() {

		return this.stringValue;
	}

	/**
	 * Sets the string value.
	 * 
	 * @param stringValue
	 */
	public void setStringValue(String stringValue) {

		this.stringValue = stringValue;
	}

	/**
	 * Returns the boolean value. Can be <code>null</code> if the {{@link #type}
	 * is different
	 * 
	 * @return boolean value
	 */
	public Boolean getBooleanValue() {

		return this.booleanValue;
	}

	/**
	 * Sets the boolean value
	 * 
	 * @param booleanValue
	 */
	public void setBooleanValue(Boolean booleanValue) {

		this.booleanValue = booleanValue;
	}

	/**
	 * Returns the date value. Can be <code>null</code> if the {{@link #type}
	 * is different
	 * 
	 * @return ate value
	 */
	public Date getDateValue() {

		return this.dateValue;
	}

	/**
	 * Sets the date value.
	 * 
	 * @param dateValue
	 */
	public void setDateValue(Date dateValue) {

		this.dateValue = dateValue;
	}

	/**
	 * Returns the double value. Can be <code>null</code> if the {{@link #type}
	 * is different
	 * 
	 * @return double value
	 */
	public Double getDoubleValue() {

		return this.doubleValue;
	}

	/**
	 * Sets the double value
	 * 
	 * @param doubleValue
	 */
	public void setDoubleValue(Double doubleValue) {

		this.doubleValue = doubleValue;
	}

	/**
	 * Returns the integer value. Can be <code>null</code> if the {{@link #type}
	 * is different
	 * 
	 * @return integer value
	 */
	public Integer getIntegerValue() {

		return this.integerValue;
	}

	/**
	 * Sets the integer value
	 * 
	 * @param integerValue
	 */
	public void setIntegerValue(Integer integerValue) {

		this.integerValue = integerValue;
	}

	/**
	 * Sets the value of this attribute. Object must be of the specified {{@link #type}.
	 * Else an {@link UnsupportedDataTypeException} will be thrown.
	 * 
	 * @param value
	 * @throws UnsupportedDataTypeException
	 */
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

	/**
	 * Returns the value of this attribute according to the attribute
	 * {@link #type}. Throws an {@link UnsupportedDataTypeException} if the
	 * {@link #type} is unsupported
	 * 
	 * @return attribute value
	 * @throws UnsupportedDataTypeException
	 */
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

	/**
	 * Returns the attribute's data type.
	 * 
	 * @return attribute data type
	 */
	public DatatypeType getType() {

		return this.type;
	}

	/**
	 * Sets the attribute's data type
	 * 
	 * @param type
	 *            data type
	 */
	public void setType(DatatypeType type) {

		this.type = type;
	}

	/**
	 * Gives back the index of this attribute. This only really makes sense in
	 * the event of multi-valued attributes. For single-valued attributes the
	 * index defaults to zero. Counting starts from zero/
	 * 
	 * @return index
	 */
	public long getIndex() {

		return this.index;
	}

	/**
	 * Sets the index of this attribute. This only really makes sense in the
	 * event of multi-valued attributes. For single-valued attributes the index
	 * defaults to zero. Counting starts from zero.
	 * 
	 * @param index
	 */
	public void setIndex(long index) {

		this.index = index;
	}

	/**
	 * Copies the attribute object.
	 */
	@Override
	public Attribute clone() {

		Attribute attribute = new Attribute(this.name, this.type, this.index);
		attribute.stringValue = this.stringValue;
		attribute.booleanValue = this.booleanValue;
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
