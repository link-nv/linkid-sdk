/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer;

import java.io.Serializable;

public class LawyerStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean lawyer;

	private boolean suspended;

	private boolean barAdmin;

	private String bar;

	public String getBar() {
		return this.bar;
	}

	public boolean isLawyer() {
		return this.lawyer;
	}

	public boolean isSuspended() {
		return this.suspended;
	}

	public boolean isBarAdmin() {
		return this.barAdmin;
	}

	public LawyerStatus() {
		this(false, false, null, false);
	}

	public void setBar(String bar) {
		this.bar = bar;
	}

	public void setBarAdmin(boolean barAdmin) {
		this.barAdmin = barAdmin;
	}

	public void setLawyer(boolean lawyer) {
		this.lawyer = lawyer;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public LawyerStatus(final boolean lawyer, final boolean suspended,
			final String bar, final boolean barAdmin) {
		super();
		this.lawyer = lawyer;
		this.suspended = suspended;
		this.bar = bar;
		this.barAdmin = barAdmin;
	}
}
