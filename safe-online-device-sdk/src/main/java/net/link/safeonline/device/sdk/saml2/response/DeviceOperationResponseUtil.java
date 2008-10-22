/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2.response;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.sdk.auth.saml2.DomUtils;
import net.link.safeonline.sdk.auth.saml2.SamlResponseSecurityPolicyResolver;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.security.SecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Utility class for validating device operation responses.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceOperationResponseUtil {

    private static final Log LOG = LogFactory.getLog(DeviceOperationResponseUtil.class);


    private DeviceOperationResponseUtil() {

        // empty
    }

    /**
     * Validates a DeviceOperationResponse in the specified HTTP request. Checks:
     * <ul>
     * <li>response ID</li>
     * <li>response validated with STS WS location</li>
     * <li>at least 1 assertion present</li>
     * <li>assertion subject</li>
     * <li>assertion conditions notOnOrAfter and notBefore
     * </ul>
     * 
     * @param now
     * @param httpRequest
     * @param expectedInResponseTo
     * @param expectedDeviceOperation
     * @param stsWsLocation
     * @param certificate
     * @param privateKey
     * @throws ServletException
     */
    public static DeviceOperationResponse validateResponse(DateTime now, HttpServletRequest httpRequest,
            String expectedInResponseTo, DeviceOperationType expectedDeviceOperation, String stsWsLocation,
            X509Certificate certificate, PrivateKey privateKey, TrustDomainType trustDomain) throws ServletException {

        if (false == "POST".equals(httpRequest.getMethod()))
            return null;
        LOG.debug("POST response");
        String encodedSamlResponse = httpRequest.getParameter("SAMLResponse");
        if (null == encodedSamlResponse) {
            LOG.debug("no SAMLResponse parameter found");
            return null;
        }
        LOG.debug("SAMLResponse parameter found");
        LOG.debug("encodedSamlResponse: " + encodedSamlResponse);

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(httpRequest));

        SecurityPolicyResolver securityPolicyResolver = new SamlResponseSecurityPolicyResolver();
        messageContext.setSecurityPolicyResolver(securityPolicyResolver);

        HTTPPostDecoder decoder = new HTTPPostDecoder();
        try {
            decoder.decode(messageContext);
        } catch (MessageDecodingException e) {
            LOG.debug("SAML message decoding error: " + e.getMessage(), e);
            throw new ServletException("SAML message decoding error");
        } catch (SecurityPolicyException e) {
            LOG.debug("security policy error: " + e.getMessage(), e);
            throw new ServletException("security policy error");
        } catch (SecurityException e) {
            LOG.debug("security error: " + e.getMessage(), e);
            throw new ServletException("security error");
        }

        SAMLObject samlMessage = messageContext.getInboundSAMLMessage();
        if (false == samlMessage instanceof DeviceOperationResponse)
            throw new ServletException("SAML message not a DeviceOperationResponse message");
        DeviceOperationResponse response = (DeviceOperationResponse) samlMessage;

        byte[] decodedSamlResponse;
        try {
            decodedSamlResponse = Base64.decode(encodedSamlResponse);
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
        SecurityTokenServiceClient stsClient = new SecurityTokenServiceClientImpl(stsWsLocation, certificate,
                privateKey);
        try {
            stsClient.validate(samlElement, trustDomain);
        } catch (RuntimeException e) {
            throw new ServletException(e.getMessage());
        } catch (WSClientTransportException e) {
            throw new ServletException(e.getMessage());
        }

        /*
         * Check whether the response is indeed a response to a previous request by comparing the InResponseTo fields
         */
        if (!response.getInResponseTo().equals(expectedInResponseTo))
            throw new ServletException("device operation response is not a response belonging to the original request.");

        if (!response.getDeviceOperation().equals(expectedDeviceOperation.name()))
            throw new ServletException(
                    "device operation response is not a response belonging to the original request, mismatch in device operation");

        if (response.getStatus().getStatusCode().getValue().equals(StatusCode.REQUEST_UNSUPPORTED_URI)
                || response.getStatus().getStatusCode().getValue().equals(DeviceOperationResponse.FAILED_URI))
            /**
             * Device Operation failed but response ok.
             */
            return response;

        if (response.getDeviceOperation().equals(DeviceOperationType.NEW_ACCOUNT_REGISTER.name())) {

            List<Assertion> assertions = response.getAssertions();
            if (assertions.isEmpty())
                throw new ServletException("missing Assertion");

            for (Assertion assertion : assertions) {
                Conditions conditions = assertion.getConditions();
                DateTime notBefore = conditions.getNotBefore();
                DateTime notOnOrAfter = conditions.getNotOnOrAfter();

                LOG.debug("now: " + now.toString());
                LOG.debug("notBefore: " + notBefore.toString());
                LOG.debug("notOnOrAfter : " + notOnOrAfter.toString());

                if (now.isBefore(notBefore) || now.isAfter(notOnOrAfter))
                    throw new ServletException("invalid SAML message timeframe");

                Subject subject = assertion.getSubject();
                if (null == subject)
                    throw new ServletException("missing Assertion Subject");

                if (assertion.getAuthnStatements().isEmpty())
                    throw new ServletException("missing AuthnStatement");

                AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
                if (null == authnStatement.getAuthnContext())
                    throw new ServletException("missing AuthnContext");

                if (null == authnStatement.getAuthnContext().getAuthnContextClassRef())
                    throw new ServletException("missing AuthnContextClassRef");
            }
        }
        return response;
    }
}
