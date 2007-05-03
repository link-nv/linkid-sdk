/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription;

import java.io.Serializable;

public class UserStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private boolean admin;

	private boolean careProvider;

	private boolean pharmacist;

	public UserStatus() {
		this(null, false, false, false);
	}

	public UserStatus(String name, boolean admin, boolean careProvider,
			boolean pharmacist) {
		super();
		this.name = name;
		this.admin = admin;
		this.careProvider = careProvider;
		this.pharmacist = pharmacist;
	}

	public boolean isAdmin() {
		return this.admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isCareProvider() {
		return this.careProvider;
	}

	public void setCareProvider(boolean careProvider) {
		this.careProvider = careProvider;
	}

	public boolean isPharmacist() {
		return this.pharmacist;
	}

	public void setPharmacist(boolean pharmacist) {
		this.pharmacist = pharmacist;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
