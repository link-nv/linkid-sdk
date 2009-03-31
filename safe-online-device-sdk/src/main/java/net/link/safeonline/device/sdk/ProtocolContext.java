/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;


public class ProtocolContext implements Serializable {

    private static final long   serialVersionUID = 1L;

    private String              device;

    private List<String>        authenticatedDevices;

    private String              subject;

    private String              attributeId;

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

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    public String getDevice() {

        return device;
    }

    public void setDevice(String device) {

        this.device = device;
    }

    public String getIssuer() {

        return issuer;
    }

    public void setIssuer(String issuer) {

        this.issuer = issuer;
    }

    public String getInResponseTo() {

        return inResponseTo;
    }

    public void setInResponseTo(String inResponseTo) {

        this.inResponseTo = inResponseTo;
    }

    public String getTargetUrl() {

        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {

        this.targetUrl = targetUrl;
    }

    public String getNodeName() {

        return nodeName;
    }

    public void setNodeName(String nodeName) {

        this.nodeName = nodeName;
    }

    public void setDeviceOperation(DeviceOperationType deviceOperation) {

        this.deviceOperation = deviceOperation;
    }

    public DeviceOperationType getDeviceOperation() {

        return deviceOperation;
    }

    public boolean getSuccess() {

        return success;
    }

    public void setSuccess(boolean success) {

        this.success = success;
    }

    public List<String> getAuthenticatedDevices() {

        return authenticatedDevices;
    }

    public void setAuthenticatedDevices(List<String> authenticatedDevices) {

        this.authenticatedDevices = authenticatedDevices;
    }

    public int getValidity() {

        return validity;
    }

    public void setValidity(int validity) {

        this.validity = validity;
    }

    public String getAttributeId() {

        return attributeId;
    }

    public void setAttributeId(String attributeId) {

        this.attributeId = attributeId;
    }

    public String getAttribute() {

        return attribute;
    }

    public void setAttribute(String attribute) {

        this.attribute = attribute;
    }
}
