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
 * application.
 * 
 * @author fcorneli
 * 
 */
public class AttributeDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String humanReadableName;

	private String description;

	private String value;

	private boolean editable;

	public AttributeDO(String name, String humanReadableName,
			String description, String value, boolean editable) {
		this.name = name;
		this.humanReadableName = humanReadableName;
		this.description = description;
		this.value = value;
		this.editable = editable;
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

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public String getHumanReadableName() {
		return this.humanReadableName;
	}

	public void setHumanReadableName(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}
}
