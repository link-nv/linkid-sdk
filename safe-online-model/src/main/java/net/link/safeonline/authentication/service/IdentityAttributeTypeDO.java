/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.io.Serializable;

/**
 * Identity Attribute Type Data Object. Used to transfer attribute type data
 * between service and user application.
 * 
 * @author fcorneli
 * 
 */
public class IdentityAttributeTypeDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private boolean required;

	private boolean dataMining;

	/**
	 * Main constructor.
	 * 
	 * @param name
	 * @param required
	 * @param dataMining
	 */
	public IdentityAttributeTypeDO(String name, boolean required,
			boolean dataMining) {
		this.name = name;
		this.required = required;
		this.dataMining = dataMining;
	}

	/**
	 * Convenience constructor. This will make for a required identity
	 * attribute.
	 * 
	 * @param name
	 */
	public IdentityAttributeTypeDO(String name) {
		this(name, true, false);
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

	/**
	 * Gives back whether this attribute is a required attribute for the
	 * identity.
	 * 
	 * @return
	 */
	public boolean isRequired() {
		return this.required;
	}

	public void setRequired(boolean editable) {
		this.required = editable;
	}

	/**
	 * Returns whether this attribute can only be accessed in an anonymous way
	 * 
	 * @return
	 */
	public boolean isDataMining() {
		return this.dataMining;
	}

	public void setDataMining(boolean dataMining) {
		this.dataMining = dataMining;
	}
}
