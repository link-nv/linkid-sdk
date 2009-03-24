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
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public abstract class RequestUtil {

    private static final Log LOG = LogFactory.getLog(RequestUtil.class);


    /**
     * Sends a SAML2 authentication or logout Request using the specified Velocity template. The SAML2 Token should already be Base64
     * encoded.
     */
    public static void sendRequest(String targetUrl, String encodedSamlRequestToken, Locale language, Integer color, Boolean minimal,
                                   String templateResourceName, HttpServletResponse httpResponse, boolean breakFrame)
            throws ServletException, IOException {

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
        velocityContext.put("BreakFrame", breakFrame);
        if (null != language) {
            velocityContext.put("Language", language.getLanguage());
        }
        if (null != color) {
            velocityContext.put("Color", String.format("#%02X%02X%02X", (color >> 16) % (0xFF + 1), (color >> 8) % (0xFF + 1), (color >> 0)
                    % (0xFF + 1)));
        }
        if (null != minimal) {
            velocityContext.put("Minimal", Boolean.toString(minimal));
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
     * @param wsLocation
     * @param applicationCertificate
     * @param applicationPrivateKey
     * @throws ServletException
     */
    public static AuthnRequest validateAuthnRequest(HttpServletRequest request, String wsLocation, X509Certificate applicationCertificate,
                                                    PrivateKey applicationPrivateKey, TrustDomainType trustDomain)
            throws ServletException {

        if (false == validateRequest(request, wsLocation, applicationCertificate, applicationPrivateKey, trustDomain))
            return null;

        return getAuthnRequest(request);

    }

    /**
     * Returns the SAML v2.0 {@link AuthnRequest} embedded in the request. Throws a {@link ServletException} if not found or of the wrong
     * type.
     * 
     * @param request
     * @throws ServletException
     */
    public static AuthnRequest getAuthnRequest(HttpServletRequest request)
            throws ServletException {

        SAMLObject samlObject = getSAMLObject(request);
        if (false == samlObject instanceof AuthnRequest)
            throw new ServletException("SAML message not an authentication request message");
        return (AuthnRequest) samlObject;
    }

    /**
     * Validates a SAML logout request in the HTTP request
     * 
     * @param request
     * @param wsLocation
     * @param applicationCertificate
     * @param applicationPrivateKey
     * @throws ServletException
     */
    public static LogoutRequest validateLogoutRequest(HttpServletRequest request, String wsLocation,
                                                      X509Certificate applicationCertificate, PrivateKey applicationPrivateKey,
                                                      TrustDomainType trustDomain)
            throws ServletException {

        if (false == validateRequest(request, wsLocation, applicationCertificate, applicationPrivateKey, trustDomain))
            return null;

        LogoutRequest logoutRequest = getLogoutRequest(request);

        if (null == logoutRequest.getNameID() || null == logoutRequest.getNameID().getValue())
            throw new ServletException("missing NameID element");

        if (null == logoutRequest.getID())
            throw new ServletException("missing ID element");

        return logoutRequest;
    }

    /**
     * Returns the SAML v2.0 {@link LogoutRequest} embedded in the request. Throws a {@link ServletException} if not found or of the wrong
     * type.
     * 
     * @param request
     * @throws ServletException
     */
    public static LogoutRequest getLogoutRequest(HttpServletRequest request)
            throws ServletException {

        SAMLObject samlObject = getSAMLObject(request);
        if (false == samlObject instanceof LogoutRequest)
            throw new ServletException("SAML message not an logout request message");
        return (LogoutRequest) samlObject;
    }

    private static SAMLObject getSAMLObject(HttpServletRequest request)
            throws ServletException {

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));

        messageContext.setSecurityPolicyResolver(new SamlRequestSecurityPolicyResolver());

        HTTPPostDecoder decoder = new HTTPPostDecoder();
        try {
            decoder.decode(messageContext);
        } catch (MessageDecodingException e) {
            LOG.debug("SAML message decoding error: " + e.getMessage());
            throw new ServletException("SAML message decoding error");
        } catch (SecurityPolicyException e) {
            LOG.debug("security policy error: " + e.getMessage());
            throw new ServletException("security policy error");
        } catch (SecurityException e) {
            LOG.debug("security error: " + e.getMessage());
            throw new ServletException("security error");
        }

        return messageContext.getInboundSAMLMessage();
    }

    /**
     * Validates the embedded SAML request token using the specified STS WS.
     */
    private static boolean validateRequest(HttpServletRequest request, String stsWsLocation, X509Certificate applicationCertificate,
                                           PrivateKey applicationPrivateKey, TrustDomainType trustDomain)
            throws ServletException {

        if (false == "POST".equals(request.getMethod()))
            return false;
        LOG.debug("POST response");
        String encodedSamlRequest = request.getParameter("SAMLRequest");
        if (null == encodedSamlRequest) {
            LOG.debug("no SAMLRequest parameter found");
            return false;
        }
        LOG.debug("encodedSamlRequest: " + encodedSamlRequest);

        byte[] decodedSamlRequest;
        try {
            decodedSamlRequest = Base64.decode(encodedSamlRequest);
        } catch (Base64DecodingException e) {
            throw new ServletException("BASE64 decoding error");
        }
        Document samlDocument;
        try {
            samlDocument = DomUtils.parseDocument(new String(decodedSamlRequest));
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
        return true;
    }

}
