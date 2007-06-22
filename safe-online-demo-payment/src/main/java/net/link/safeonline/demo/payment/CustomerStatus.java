/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment;

import java.io.Serializable;

public class CustomerStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean junior;

	private boolean paymentAdmin;

	public boolean isJunior() {
		return this.junior;
	}

	public boolean isPaymentAdmin() {
		return this.paymentAdmin;
	}

	public CustomerStatus() {
		this(false, false);
	}

	public void setPaymentAdmin(boolean paymentAdmin) {
		this.paymentAdmin = paymentAdmin;
	}

	public void setJunior(boolean junior) {
		this.junior = junior;
	}

	public CustomerStatus(final boolean junior, final boolean paymentAdmin) {
		super();
		this.junior = junior;
		this.paymentAdmin = paymentAdmin;
	}
}
