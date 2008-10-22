/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.exception.DeviceFinalizationException;
import net.link.safeonline.device.sdk.exception.DeviceInitializationException;
import net.link.safeonline.device.sdk.saml2.request.DeviceOperationRequest;
import net.link.safeonline.device.sdk.saml2.request.DeviceOperationRequestUtil;
import net.link.safeonline.device.sdk.saml2.response.DeviceOperationResponseFactory;
import net.link.safeonline.sdk.auth.saml2.ResponseUtil;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;


/**
 * SAML handler used by remote device issuers to handle an incoming SAML authentication request used for registration,
 * updating or removal and store the retrieved information on the session into {@link ProtocolContext}.
 * 
 * After registrating, updating or removing it will post a SAML authentication response containing the necessary
 * assertions or a SAML authentication response telling the authentication has failed.
 * 
 * @author wvdhaute
 * 
 */
public class Saml2Handler implements Serializable {

    private static final long   serialVersionUID               = 1L;

    private static final Log    LOG                            = LogFactory.getLog(Saml2Handler.class);
    
    private static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/device/sdk/saml2/binding/saml2-post-binding.vm";

    private String              stsWsLocation;

    private String              issuer;

    private KeyPair             applicationKeyPair;

    private X509Certificate     applicationCertificate;

    private static final String SAML2_HANDLER                  = Saml2Handler.class.getName() + ".SAML2_HANDLER";

    static {
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException("could not bootstrap the OpenSAML2 library");
        }
    }


    private Saml2Handler() {

    }

    public static Saml2Handler getSaml2Handler(HttpServletRequest request) {

        Saml2Handler instance = (Saml2Handler) request.getSession().getAttribute(SAML2_HANDLER);
        if (null == instance) {
            instance = new Saml2Handler();
            request.getSession().setAttribute(SAML2_HANDLER, instance);
        }
        return instance;
    }

    public static Saml2Handler findSaml2Handler(HttpServletRequest request) {

        Saml2Handler instance = (Saml2Handler) request.getSession().getAttribute(SAML2_HANDLER);
        return instance;
    }

    public void init(Map<String, String> configParams, X509Certificate newApplicationCertificate,
            KeyPair newApplicationKeyPair) throws DeviceInitializationException {

        this.stsWsLocation = configParams.get("StsWsLocation");
        this.issuer = configParams.get("DeviceName");
        this.applicationCertificate = newApplicationCertificate;
        this.applicationKeyPair = newApplicationKeyPair;
        if (null == this.stsWsLocation)
            throw new DeviceInitializationException("Missing STS WS Location ( \"StsWsLocation\" )");
    }

    public DeviceOperationType initDeviceOperation(HttpServletRequest request) throws DeviceInitializationException {

        DeviceOperationRequest deviceOperationRequest;
        try {
            deviceOperationRequest = DeviceOperationRequestUtil.validateRequest(request, this.stsWsLocation,
                    this.applicationCertificate, this.applicationKeyPair.getPrivate(), TrustDomainType.NODE);
        } catch (ServletException e) {
            throw new DeviceInitializationException(e.getMessage());
        }

        String serviceURL = deviceOperationRequest.getServiceURL();
        if (null == serviceURL)
            throw new DeviceInitializationException("missing ServiceURL");
        LOG.debug("serviceURL: " + serviceURL);

        String nodeName = deviceOperationRequest.getIssuer().getValue();
        LOG.debug("node name: " + nodeName);

        String deviceOperationRequestId = deviceOperationRequest.getID();

        String device = deviceOperationRequest.getDevice();
        LOG.debug("device: " + device);

        String authenticatedDevice = deviceOperationRequest.getAuthenticatedDevice();
        LOG.debug("authenticated device: " + authenticatedDevice);

        DeviceOperationType deviceOperation = DeviceOperationType.valueOf(deviceOperationRequest.getDeviceOperation());
        LOG.debug("device operation: " + deviceOperation);

        String attribute = deviceOperationRequest.getAttribute();
        LOG.debug("attribute: " + attribute);

        if (null == deviceOperationRequest.getSubject())
            throw new DeviceInitializationException("missing subject");
        if (null == deviceOperationRequest.getSubject().getNameID())
            throw new DeviceInitializationException("missing subject name ID");
        String userId = deviceOperationRequest.getSubject().getNameID().getValue();
        LOG.debug("user id: " + userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        protocolContext.setIssuer(this.issuer);
        protocolContext.setTargetUrl(serviceURL);
        protocolContext.setInResponseTo(deviceOperationRequestId);
        protocolContext.setDevice(device);
        protocolContext.setAuthenticatedDevice(authenticatedDevice);
        protocolContext.setSubject(userId);
        protocolContext.setNodeName(nodeName);
        protocolContext.setDeviceOperation(deviceOperation);
        protocolContext.setAttribute(attribute);

        DeviceOperationManager.setUserId(userId, request);
        DeviceOperationManager.setOperation(deviceOperation.name(), request);
        DeviceOperationManager.setAuthenticatedDevice(authenticatedDevice, request);
        DeviceOperationManager.setAttribute(attribute, request);
        
        return deviceOperation;
    }

    public void abortDeviceOperation(HttpServletRequest request, HttpServletResponse response)
            throws DeviceFinalizationException {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        String inResponseTo = protocolContext.getInResponseTo();
        if (null == inResponseTo)
            throw new DeviceFinalizationException("missing IN_RESPONSE_TO session attribute");

        String samlResponseToken = DeviceOperationResponseFactory.createDeviceOperationResponse(inResponseTo,
                protocolContext.getDeviceOperation(), protocolContext.getIssuer(), protocolContext.getSubject(),
                protocolContext.getDevice(), this.applicationKeyPair, protocolContext.getValidity(), protocolContext
                        .getTargetUrl());

        String encodedSamlResponseToken = Base64.encode(samlResponseToken.getBytes());

        String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

        try {
            ResponseUtil.sendResponse(encodedSamlResponseToken, templateResourceName, protocolContext.getTargetUrl(),
                    response);
        } catch (ServletException e) {
            throw new DeviceFinalizationException(e.getMessage());
        } catch (IOException e) {
            throw new DeviceFinalizationException(e.getMessage());
        }
    }

    public void finalizeDeviceOperation(HttpServletRequest request, HttpServletResponse response)
            throws DeviceFinalizationException {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        boolean deviceOperationSuccess = protocolContext.getSuccess();
        String inResponseTo = protocolContext.getInResponseTo();
        if (null == inResponseTo)
            throw new DeviceFinalizationException("missing IN_RESPONSE_TO session attribute");

        String samlResponseToken;
        if (!deviceOperationSuccess) {
            /*
             * Device operation have failed
             */
            samlResponseToken = DeviceOperationResponseFactory.createDeviceOperationResponseFailed(inResponseTo,
                    protocolContext.getDeviceOperation(), protocolContext.getIssuer(), protocolContext.getSubject(),
                    protocolContext.getDevice(), this.applicationKeyPair, protocolContext.getValidity(),
                    protocolContext.getTargetUrl());
        } else {
            /*
             * Device operation was successful
             */
            samlResponseToken = DeviceOperationResponseFactory.createDeviceOperationResponse(inResponseTo,
                    protocolContext.getDeviceOperation(), protocolContext.getIssuer(), protocolContext.getSubject(),
                    protocolContext.getDevice(), this.applicationKeyPair, protocolContext.getValidity(),
                    protocolContext.getTargetUrl());
        }

        String encodedSamlResponseToken = Base64.encode(samlResponseToken.getBytes());

        String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

        try {
            ResponseUtil.sendResponse(encodedSamlResponseToken, templateResourceName, protocolContext.getTargetUrl(),
                    response);
        } catch (ServletException e) {
            throw new DeviceFinalizationException(e.getMessage());
        } catch (IOException e) {
            throw new DeviceFinalizationException(e.getMessage());
        }

        // destroy the session to prevent reuse
        request.getSession().invalidate();
    }
}
