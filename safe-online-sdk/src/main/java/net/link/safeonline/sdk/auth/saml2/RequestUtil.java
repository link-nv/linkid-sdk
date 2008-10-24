/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.security.SecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Utility class for SAML2 authentication requests.
 * 
 * @author wvdhaute
 * 
 */
public class RequestUtil {

    private RequestUtil() {

        // empty
    }

    /**
     * Sends a SAML2 authentication or logout Request using the specified Velocity template. The SAML2 Token should already be Base64
     * encoded.
     * 
     * @param targetUrl
     * @param encodedSamlRequestToken
     * @param templateResourceName
     * @param httpResponse
     * @throws ServletException
     * @throws IOException
     */
    public static void sendRequest(String targetUrl, String encodedSamlRequestToken, String templateResourceName,
                                   HttpServletResponse httpResponse) throws ServletException, IOException {

        sendRequest(targetUrl, encodedSamlRequestToken, null, templateResourceName, httpResponse);
    }

    /**
     * Sends a SAML2 authentication or logout Request using the specified Velocity template. The SAML2 Token should already be Base64
     * encoded.
     * 
     * @param targetUrl
     * @param encodedSamlRequestToken
     * @param language
     * @param templateResourceName
     * @param httpResponse
     * @throws ServletException
     * @throws IOException
     */
    public static void sendRequest(String targetUrl, String encodedSamlRequestToken, String language, String templateResourceName,
                                   HttpServletResponse httpResponse) throws ServletException, IOException {

        /*
         * We could use the opensaml2 HTTPPostEncoderBuilder here to construct the HTTP response. But this code is just too complex in
         * usage. It's easier to do all these things ourselves.
         */
        Properties velocityProperties = new Properties();
        velocityProperties.put("resource.loader", "class");
        velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
        velocityProperties.put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, RequestUtil.class.getName());
        velocityProperties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VelocityEngine velocityEngine;
        try {
            velocityEngine = new VelocityEngine(velocityProperties);
            velocityEngine.init();
        } catch (Exception e) {
            throw new ServletException("could not initialize velocity engine");
        }
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("action", targetUrl);
        velocityContext.put("SAMLRequest", encodedSamlRequestToken);
        if (null != language) {
            velocityContext.put("Language", language);
        }

        Template template;
        try {
            template = velocityEngine.getTemplate(templateResourceName);
        } catch (Exception e) {
            throw new ServletException("Velocity template error: " + e.getMessage(), e);
        }

        httpResponse.setContentType("text/html");
        PrintWriter out = httpResponse.getWriter();
        template.merge(velocityContext, out);
    }

    /**
     * Validates a SAML request in the HTTP request
     * 
     * @param request
     * @param stsWsLocation
     * @param applicationCertificate
     * @param applicationPrivateKey
     * @throws ServletException
     */
    public static AuthnRequest validateAuthnRequest(HttpServletRequest request, String stsWsLocation,
                                                    X509Certificate applicationCertificate, PrivateKey applicationPrivateKey,
                                                    TrustDomainType trustDomain) throws ServletException {

        String encodedSamlRequest = request.getParameter("SAMLRequest");
        if (null == encodedSamlRequest)
            throw new ServletException("no SAML request found");

        byte[] decodedSamlResponse;
        try {
            decodedSamlResponse = Base64.decode(encodedSamlRequest);
        } catch (Base64DecodingException e) {
            throw new ServletException("BASE64 decoding error");
        }
        Document samlDocument;
        try {
            samlDocument = DomUtils.parseDocument(new String(decodedSamlResponse));
        } catch (Exception e) {
            throw new ServletException("DOM parsing error");
        }
        Element samlElement = samlDocument.getDocumentElement();
        SecurityTokenServiceClient stsClient = new SecurityTokenServiceClientImpl(stsWsLocation, applicationCertificate,
                applicationPrivateKey);
        try {
            stsClient.validate(samlElement, trustDomain);
        } catch (RuntimeException e) {
            throw new ServletException(e.getMessage());
        } catch (WSClientTransportException e) {
            throw new ServletException(e.getMessage());
        }

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));

        messageContext.setSecurityPolicyResolver(new SamlRequestSecurityPolicyResolver());

        HTTPPostDecoder decoder = new HTTPPostDecoder();
        try {
            decoder.decode(messageContext);
        } catch (MessageDecodingException e) {
            throw new ServletException("SAML message decoding error");
        } catch (SecurityPolicyException e) {
            throw new ServletException("security policy error");
        } catch (SecurityException e) {
            throw new ServletException("security error");
        }

        SAMLObject samlMessage = messageContext.getInboundSAMLMessage();
        if (false == samlMessage instanceof AuthnRequest) {
            throw new ServletException("SAML message not an authentication request message");
        }
        AuthnRequest samlAuthnRequest = (AuthnRequest) samlMessage;
        return samlAuthnRequest;
    }

    /**
     * Validates a SAML logout request in the HTTP request
     * 
     * @param request
     * @param stsWsLocation
     * @param applicationCertificate
     * @param applicationPrivateKey
     * @throws ServletException
     */
    public static LogoutRequest validateLogoutRequest(HttpServletRequest request, String stsWsLocation,
                                                      X509Certificate applicationCertificate, PrivateKey applicationPrivateKey,
                                                      TrustDomainType trustDomain) throws ServletException {

        String encodedSamlRequest = request.getParameter("SAMLRequest");
        if (null == encodedSamlRequest)
            throw new ServletException("no SAML request found");

        byte[] decodedSamlResponse;
        try {
            decodedSamlResponse = Base64.decode(encodedSamlRequest);
        } catch (Base64DecodingException e) {
            throw new ServletException("BASE64 decoding error");
        }
        Document samlDocument;
        try {
            samlDocument = DomUtils.parseDocument(new String(decodedSamlResponse));
        } catch (Exception e) {
            throw new ServletException("DOM parsing error");
        }
        Element samlElement = samlDocument.getDocumentElement();
        SecurityTokenServiceClient stsClient = new SecurityTokenServiceClientImpl(stsWsLocation, applicationCertificate,
                applicationPrivateKey);
        try {
            stsClient.validate(samlElement, trustDomain);
        } catch (RuntimeException e) {
            throw new ServletException(e.getMessage());
        } catch (WSClientTransportException e) {
            throw new ServletException(e.getMessage());
        }

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));

        messageContext.setSecurityPolicyResolver(new SamlRequestSecurityPolicyResolver());

        HTTPPostDecoder decoder = new HTTPPostDecoder();
        try {
            decoder.decode(messageContext);
        } catch (MessageDecodingException e) {
            throw new ServletException("SAML message decoding error");
        } catch (SecurityPolicyException e) {
            throw new ServletException("security policy error");
        } catch (SecurityException e) {
            throw new ServletException("security error");
        }

        SAMLObject samlMessage = messageContext.getInboundSAMLMessage();
        if (false == samlMessage instanceof LogoutRequest) {
            throw new ServletException("SAML message not an authentication request message");
        }
        LogoutRequest samlLogoutRequest = (LogoutRequest) samlMessage;

        if (null == samlLogoutRequest.getNameID() || null == samlLogoutRequest.getNameID().getValue())
            throw new ServletException("missing NameID element");

        if (null == samlLogoutRequest.getID())
            throw new ServletException("missing ID element");

        return samlLogoutRequest;
    }
}
