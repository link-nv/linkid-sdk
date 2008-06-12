/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import java.io.Serializable;
import java.util.Set;

import javax.servlet.http.HttpSession;

public class AuthenticationContext implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String AUTHENTICATION_CONTEXT = "AuthenticationContext";

	private String inResponseTo;

	private String targetUrl;

	private Set<String> wantedDevices;

	private String usedDevice;

	private String userId;

	private String nodeName;

	private String application;

	private String issuer;

	private int validity;

	public static AuthenticationContext getAuthenticationContext(
			HttpSession session) {
		AuthenticationContext instance = (AuthenticationContext) session
				.getAttribute(AUTHENTICATION_CONTEXT);
		if (null == instance) {
			instance = new AuthenticationContext();
			session.setAttribute(AUTHENTICATION_CONTEXT, instance);
		}
		return instance;
	}

	public Set<String> getWantedDevices() {
		return this.wantedDevices;
	}

	public void setWantedDevices(Set<String> wantedDevices) {
		this.wantedDevices = wantedDevices;
	}

	public String getUsedDevice() {
		return this.usedDevice;
	}

	public void setUsedDevice(String usedDevice) {
		this.usedDevice = usedDevice;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNodeName() {
		return this.nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
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

	public String getInResponseTo() {
		return this.inResponseTo;
	}

	public void setInResponseTo(String inResponseTo) {
		this.inResponseTo = inResponseTo;
	}

	public String getTargetUrl() {
		return this.targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
}
