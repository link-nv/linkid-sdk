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

	private final boolean lawyer;

	private final boolean suspended;

	private final String bar;

	public String getBar() {
		return bar;
	}

	public boolean isLawyer() {
		return lawyer;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public LawyerStatus() {
		this(false, false, null);
	}

	public LawyerStatus(final boolean lawyer, final boolean suspended,
			final String bar) {
		super();
		this.lawyer = lawyer;
		this.suspended = suspended;
		this.bar = bar;
	}
}
