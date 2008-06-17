/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.saml2.DeviceOperationType;

public class ProtocolContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private String wantedDevice;

	private String subject;

	private DeviceOperationType deviceOperation;

	private String issuer;

	private String inResponseTo;

	private String targetUrl;

	private String nodeName;

	private int validity;

	private boolean success = false;

	public static final String PROTOCOL_CONTEXT = "ProtocolContext";

	private ProtocolContext() {
	}

	public static ProtocolContext getProtocolContext(HttpSession session) {
		ProtocolContext instance = (ProtocolContext) session
				.getAttribute(PROTOCOL_CONTEXT);
		if (null == instance) {
			instance = new ProtocolContext();
			session.setAttribute(PROTOCOL_CONTEXT, instance);
		}
		return instance;
	}

	public static void removeProtocolContext(HttpSession session) {
		ProtocolContext instance = (ProtocolContext) session
				.getAttribute(PROTOCOL_CONTEXT);
		if (null != instance) {
			session.removeAttribute(PROTOCOL_CONTEXT);
		}
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getWantedDevice() {
		return this.wantedDevice;
	}

	public void setWantedDevice(String wantedDevice) {
		this.wantedDevice = wantedDevice;
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

	public String getNodeName() {
		return this.nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int getValidity() {
		return this.validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}

	public void setDeviceOperation(DeviceOperationType deviceOperation) {
		this.deviceOperation = deviceOperation;
	}

	public DeviceOperationType getDeviceOperation() {
		return this.deviceOperation;
	}

	public boolean getSuccess() {
		return this.success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
