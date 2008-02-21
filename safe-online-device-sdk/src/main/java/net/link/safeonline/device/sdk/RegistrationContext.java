/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

public class RegistrationContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private HttpSession session;

	private String registeredDevice;

	private String userId;

	private String application;

	private String issuer;

	private int validity;

	public static final String REGISTRATION_CONTEXT = RegistrationContext.class
			.getName()
			+ ".LOGIN_MANAGER";

	private RegistrationContext(HttpSession session) {
		this.session = session;
		this.session.setAttribute(REGISTRATION_CONTEXT, this);
	}

	public static RegistrationContext getRegistrationContext(HttpSession session) {
		RegistrationContext instance = (RegistrationContext) session
				.getAttribute(REGISTRATION_CONTEXT);
		if (null == instance) {
			instance = new RegistrationContext(session);
		}
		return instance;
	}

	public String getRegisteredDevice() {
		return this.registeredDevice;
	}

	public void setRegisteredDevice(String registeredDevice) {
		this.registeredDevice = registeredDevice;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getIssuer() {
		return this.issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public int getValidity() {
		return this.validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}
}
