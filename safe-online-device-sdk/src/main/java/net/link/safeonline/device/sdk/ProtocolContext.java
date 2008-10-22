/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import net.link.safeonline.device.sdk.saml2.DeviceOperationType;


public class ProtocolContext implements Serializable {

    private static final long   serialVersionUID = 1L;

    private String              device;

    private String              authenticatedDevice;

    private String              subject;

    private String              attribute;

    private DeviceOperationType deviceOperation;

    private String              issuer;

    private String              inResponseTo;

    private String              targetUrl;

    private String              nodeName;

    private int                 validity;

    private boolean             success          = false;

    public static final String  PROTOCOL_CONTEXT = "ProtocolContext";


    private ProtocolContext() {

    }

    public static ProtocolContext getProtocolContext(HttpSession session) {

        ProtocolContext instance = (ProtocolContext) session.getAttribute(PROTOCOL_CONTEXT);
        if (null == instance) {
            instance = new ProtocolContext();
            session.setAttribute(PROTOCOL_CONTEXT, instance);
        }
        return instance;
    }

    public String getSubject() {

        return this.subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    public String getDevice() {

        return this.device;
    }

    public void setDevice(String device) {

        this.device = device;
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

    public String getAuthenticatedDevice() {

        return this.authenticatedDevice;
    }

    public void setAuthenticatedDevice(String authenticatedDevice) {

        this.authenticatedDevice = authenticatedDevice;
    }

    public int getValidity() {

        return this.validity;
    }

    public void setValidity(int validity) {

        this.validity = validity;
    }

    public String getAttribute() {

        return this.attribute;
    }

    public void setAttribute(String attribute) {

        this.attribute = attribute;
    }
}
