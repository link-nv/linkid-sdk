/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import java.io.Serializable;

public class DeviceAttribute implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private boolean included;

	public DeviceAttribute(String name) {
		this.name = name;
		this.included = false;
	}

	public DeviceAttribute(String name, boolean included) {
		this.name = name;
		this.included = included;
	}

	public boolean isIncluded() {
		return this.included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public String getName() {
		return this.name;
	}

}
