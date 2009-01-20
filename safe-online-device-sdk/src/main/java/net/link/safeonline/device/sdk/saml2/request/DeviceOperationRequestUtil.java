/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2.request;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.auth.saml2.SamlRequestSecurityPolicyResolver;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.security.SecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Utility class for device operation requests.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceOperationRequestUtil {

    private DeviceOperationRequestUtil() {

        // empty
    }

    /**
     * Validates a device operation request in the HTTP request
     * 
     * @param request
     * @param stsWsLocation
     * @param applicationCertificate
     * @param applicationPrivateKey
     * @throws ServletException
     */
    public static DeviceOperationRequest validateRequest(HttpServletRequest request, String stsWsLocation,
                                                         X509Certificate applicationCertificate, PrivateKey applicationPrivateKey,
                                                         TrustDomainType trustDomain)
            throws ServletException {

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
        if (false == samlMessage instanceof DeviceOperationRequest)
            throw new ServletException("SAML message not an device operation request message");
        DeviceOperationRequest deviceOperationRequest = (DeviceOperationRequest) samlMessage;
        return deviceOperationRequest;
    }
}
