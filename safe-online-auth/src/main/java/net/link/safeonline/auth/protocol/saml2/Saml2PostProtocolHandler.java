/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol.saml2;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.LogoutServiceManager;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandler;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AuthenticationInitializationException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponse;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponseUtil;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.sdk.auth.saml2.RequestUtil;
import net.link.safeonline.sdk.auth.saml2.ResponseUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;


/**
 * Server-side protocol handler for the SAML2 Browser POST authentication protocol.
 * 
 * @author fcorneli
 * 
 */
public class Saml2PostProtocolHandler implements ProtocolHandler {

    private static final Log   LOG                            = LogFactory.getLog(Saml2PostProtocolHandler.class);

    public static final String NAME                           = "SAML v2 Browser POST Authentication Protocol";

    public static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/device/sdk/saml2/binding/saml2-post-binding.vm";

    static {
        System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException("could not bootstrap the OpenSAML2 library");
        }
    }


    /**
     * {@inheritDoc}
     */
    public String getName() {

        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public ProtocolContext handleAuthnRequest(HttpServletRequest request, Locale language, Integer color, Boolean minimal)
            throws ProtocolException {

        LOG.debug("request method: " + request.getMethod());
        if (false == "POST".equals(request.getMethod()))
            return null;
        LOG.debug("POST request");
        LOG.debug("Language parameter: " + language);

        AuthnRequest authnRequest;
        try {
            authnRequest = RequestUtil.getAuthnRequest(request);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request.getSession());
        try {
            return authenticationService.initialize(language, color, minimal, authnRequest);
        } catch (TrustDomainNotFoundException e) {
            LOG.debug("trust domain not found: " + e.getMessage());
            throw new ProtocolException("Trust domain not found", e);
        } catch (AuthenticationInitializationException e) {
            LOG.debug("authentication intialization error: " + e.getMessage());
            throw new ProtocolException("authentication intialization error: " + e.getMessage(), e);
        } catch (ApplicationNotFoundException e) {
            LOG.debug("application not found: " + e.getMessage());
            throw new ProtocolException("application not found", e);
        } catch (SignatureValidationException e) {
            LOG.debug("signature validation error: " + e.getMessage());
            throw new ProtocolException("signature validation error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationAssertion handleDeviceAuthnResponse(HttpServletRequest request)
            throws ProtocolException {

        LOG.debug("request method: " + request.getMethod());
        if (false == "POST".equals(request.getMethod()))
            return null;
        LOG.debug("POST request");

        Response response;
        try {
            response = ResponseUtil.getAuthnResponse(request);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request.getSession());
        try {
            return authenticationService.authenticate(response);
        } catch (TrustDomainNotFoundException e) {
            LOG.debug("trust domain not found: " + e.getMessage());
            throw new ProtocolException("Trust domain not found", e);
        } catch (ApplicationNotFoundException e) {
            LOG.debug("application not found: " + e.getMessage());
            throw new ProtocolException("application not found", e);
        } catch (SignatureValidationException e) {
            LOG.debug("signature validation error: " + e.getMessage());
            throw new ProtocolException("signature validation error", e);
        } catch (SubjectNotFoundException e) {
            LOG.debug("subject not found: " + e.getMessage());
            throw new ProtocolException("subject not found", e);
        } catch (NodeNotFoundException e) {
            LOG.debug("node not found: " + e.getMessage());
            throw new ProtocolException("node not found", e);
        } catch (NodeMappingNotFoundException e) {
            LOG.debug("node mapping not found: " + e.getMessage());
            throw new ProtocolException("node mapping not found", e);
        } catch (DeviceNotFoundException e) {
            LOG.debug("device not found: " + e.getMessage());
            throw new ProtocolException("device not found", e);
        } catch (ServletException e) {
            LOG.debug("error: " + e.getMessage());
            throw new ProtocolException("error: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationAssertion handleDeviceRegistrationResponse(HttpServletRequest request)
            throws ProtocolException {

        LOG.debug("request method: " + request.getMethod());
        if (false == "POST".equals(request.getMethod()))
            return null;
        LOG.debug("POST request");

        DeviceOperationResponse response;
        try {
            response = DeviceOperationResponseUtil.getDeviceOperationResponse(request);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request.getSession());
        try {
            return authenticationService.register(response);
        } catch (TrustDomainNotFoundException e) {
            LOG.debug("trust domain not found: " + e.getMessage());
            throw new ProtocolException("Trust domain not found", e);
        } catch (ApplicationNotFoundException e) {
            LOG.debug("application not found: " + e.getMessage());
            throw new ProtocolException("application not found", e);
        } catch (SignatureValidationException e) {
            LOG.debug("signature validation error: " + e.getMessage());
            throw new ProtocolException("signature validation error", e);
        } catch (SubjectNotFoundException e) {
            LOG.debug("subject not found: " + e.getMessage());
            throw new ProtocolException("subject not found", e);
        } catch (NodeNotFoundException e) {
            LOG.debug("node not found: " + e.getMessage());
            throw new ProtocolException("node not found", e);
        } catch (NodeMappingNotFoundException e) {
            LOG.debug("node mapping not found: " + e.getMessage());
            throw new ProtocolException("node mapping not found", e);
        } catch (DeviceNotFoundException e) {
            LOG.debug("device not found: " + e.getMessage());
            throw new ProtocolException("device not found", e);
        } catch (ServletException e) {
            LOG.debug("error: " + e.getMessage());
            throw new ProtocolException("error: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendAuthnResponse(HttpSession session, HttpServletResponse authnResponse)
            throws ProtocolException {

        String target = LoginManager.getTarget(session);

        String encodedSamlResponseToken;
        try {
            encodedSamlResponseToken = AuthenticationServiceManager.finalizeAuthentication(session);
        } catch (NodeNotFoundException e) {
            throw new ProtocolException("Node not found: " + e.getMessage());
        } catch (ApplicationNotFoundException e) {
            throw new ProtocolException("Application not found: " + e.getMessage());
        } catch (SubjectNotFoundException e) {
            throw new ProtocolException("Subject not found: " + e.getMessage());
        }

        String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

        try {
            ResponseUtil.sendResponse(encodedSamlResponseToken, templateResourceName, target, authnResponse, true);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage());
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public LogoutProtocolContext handleLogoutRequest(HttpServletRequest request)
            throws ProtocolException {

        LOG.debug("request method: " + request.getMethod());
        if (false == "POST".equals(request.getMethod()))
            return null;
        LOG.debug("POST request");

        LogoutRequest logoutRequest;
        try {
            logoutRequest = RequestUtil.getLogoutRequest(request);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        LogoutService logoutService = LogoutServiceManager.getLogoutService(request.getSession());
        try {
            return logoutService.initialize(logoutRequest);
        } catch (TrustDomainNotFoundException e) {
            LOG.debug("trust domain not found: " + e.getMessage());
            throw new ProtocolException("Trust domain not found");
        } catch (SignatureValidationException e) {
            LOG.debug("authentication intialization error: " + e.getMessage());
            throw new ProtocolException("authentication intialization error: " + e.getMessage());
        } catch (ApplicationNotFoundException e) {
            LOG.debug("application not found: " + e.getMessage());
            throw new ProtocolException("application not found");
        } catch (SubjectNotFoundException e) {
            LOG.debug("subject not found: " + e.getMessage());
            throw new ProtocolException("subject not found");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String handleLogoutResponse(HttpServletRequest request)
            throws ProtocolException {

        LOG.debug("request method: " + request.getMethod());
        if (false == "POST".equals(request.getMethod()))
            return null;
        LOG.debug("POST request");

        LogoutResponse logoutResponse;
        try {
            logoutResponse = ResponseUtil.getLogoutResponse(request);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage(), e);
        }

        LogoutService logoutService = LogoutServiceManager.getLogoutService(request.getSession());
        String applicationName;
        try {
            applicationName = logoutService.handleLogoutResponse(logoutResponse);
        } catch (NodeNotFoundException e) {
            throw new ProtocolException("Node not found: " + e.getMessage());
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage());
        } catch (TrustDomainNotFoundException e) {
            LOG.debug("trust domain not found: " + e.getMessage());
            throw new ProtocolException("Trust domain not found");
        } catch (SignatureValidationException e) {
            LOG.debug("authentication intialization error: " + e.getMessage());
            throw new ProtocolException("authentication intialization error: " + e.getMessage());
        } catch (ApplicationNotFoundException e) {
            LOG.debug("application not found: " + e.getMessage());
            throw new ProtocolException("application not found");
        }
        LOG.debug("application: " + applicationName);
        return applicationName;
    }

    /**
     * {@inheritDoc}
     */
    public void sendLogoutRequest(ApplicationEntity application, HttpSession session, HttpServletResponse response)
            throws ProtocolException {

        String target = application.getSsoLogoutUrl().toString();

        LogoutService logoutService = LogoutServiceManager.getLogoutService(session);
        String encodedSamlLogoutRequestToken;
        try {
            encodedSamlLogoutRequestToken = logoutService.getLogoutRequest(application);
        } catch (NodeNotFoundException e) {
            throw new ProtocolException("Node not found: " + e.getMessage());
        } catch (ApplicationNotFoundException e) {
            throw new ProtocolException("Application not found: " + e.getMessage());
        }

        String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

        try {
            RequestUtil.sendRequest(target, encodedSamlLogoutRequestToken, null, null, null, templateResourceName, response, false);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage());
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    public void sendLogoutResponse(boolean partialLogout, String target, HttpSession session, HttpServletResponse logoutResponse)
            throws ProtocolException {

        String encodedSamlLogoutResponseToken;
        try {
            encodedSamlLogoutResponseToken = LogoutServiceManager.finalizeLogout(session);
        } catch (NodeNotFoundException e) {
            throw new ProtocolException("Node not found: " + e.getMessage());
        }

        String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

        try {
            ResponseUtil.sendResponse(encodedSamlLogoutResponseToken, templateResourceName, target, logoutResponse, true);
        } catch (ServletException e) {
            throw new ProtocolException(e.getMessage());
        } catch (IOException e) {
            throw new ProtocolException(e.getMessage());
        }

    }
}
