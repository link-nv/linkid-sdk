/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

public class ProtocolContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private HttpSession session;

	private String deviceName;

	private String mappingId;

	private String application;

	private String issuer;

	private String inResponseTo;

	private String targetUrl;

	private int validity;

	public static final String PROTOCOL_CONTEXT = ProtocolContext.class
			.getName()
			+ ".LOGIN_MANAGER";

	private ProtocolContext(HttpSession session) {
		this.session = session;
		this.session.setAttribute(PROTOCOL_CONTEXT, this);
	}

	public static ProtocolContext getProtocolContext(HttpSession session) {
		ProtocolContext instance = (ProtocolContext) session
				.getAttribute(PROTOCOL_CONTEXT);
		if (null == instance) {
			instance = new ProtocolContext(session);
		}
		return instance;
	}

	public String getDeviceName() {
		return this.deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getMappingId() {
		return this.mappingId;
	}

	public void setMappingId(String mappingId) {
		this.mappingId = mappingId;
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

	public int getValidity() {
		return this.validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}
}
