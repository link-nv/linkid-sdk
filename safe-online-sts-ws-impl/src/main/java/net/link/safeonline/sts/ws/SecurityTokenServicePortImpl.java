/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sts.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;

import net.link.safeonline.device.sdk.operation.saml2.request.DeviceOperationRequest;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponse;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ValidateTargetType;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Element;


/**
 * Implementation of WS-Trust 1.3 STS JAX-WS web service endpoint. Beware that we validate both the WS-Security and SAML token signature via
 * SOAP handlers. The signature validation cannot be done within the endpoint implementation since JAXB somehow breaks the signature
 * digests.
 * 
 * @author fcorneli
 */
@WebService(endpointInterface = "org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort")
@HandlerChain(file = "sts-ws-handlers.xml")
@Injection
public class SecurityTokenServicePortImpl implements SecurityTokenServicePort {

    private static final Log   LOG                = LogFactory.getLog(SecurityTokenServicePortImpl.class);

    private final static QName TOKEN_TYPE_QNAME   = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "TokenType");

    private final static QName REQUEST_TYPE_QNAME = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "RequestType");

    @Resource
    private WebServiceContext  context;


    public RequestSecurityTokenResponseType requestSecurityToken(RequestSecurityTokenType request) {

        LOG.debug("request security token");
        String requestType = null;
        String tokenType = null;
        ValidateTargetType validateTarget = null;
        List<Object> content = request.getAny();
        for (Object contentObject : content) {
            if (contentObject instanceof JAXBElement<?>) {
                JAXBElement<?> contentElement = (JAXBElement<?>) contentObject;
                Object value = contentElement.getValue();
                if (value instanceof String) {
                    QName elementName = contentElement.getName();
                    if (TOKEN_TYPE_QNAME.equals(elementName)) {
                        tokenType = (String) value;
                    } else if (REQUEST_TYPE_QNAME.equals(elementName)) {
                        requestType = (String) value;
                    }
                } else if (value instanceof ValidateTargetType) {
                    validateTarget = (ValidateTargetType) value;
                }
            }
        }
        if (null == requestType)
            throw new RuntimeException("RequestType is required");
        if (false == requestType.startsWith("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate"))
            throw new RuntimeException("only supporting the validation binding");
        if (null != tokenType && false == SecurityTokenServiceConstants.TOKEN_TYPE_STATUS.equals(tokenType)) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "optional TokenType should be Status");
            return response;
        }
        if (null == validateTarget) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "ValidateTarget is required");
            return response;
        }
        Element tokenElement = (Element) validateTarget.getAny();
        if (null == tokenElement) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "missing token to validate");
            return response;
        }

        boolean result = TokenValidationHandler.getValidity(context);
        if (false == result) {
            LOG.debug("token signature not valid");
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "token signature not valid");
            return response;
        }

        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(tokenElement);
        XMLObject tokenXmlObject;
        try {
            tokenXmlObject = unmarshaller.unmarshall(tokenElement);
        } catch (UnmarshallingException e) {
            LOG.debug("error parsing token: " + e.getMessage(), e);
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID, "error parsing token");
            return response;
        }

        if (tokenXmlObject instanceof Response) {
            RequestSecurityTokenResponseType response = validateSaml2Response((Response) tokenXmlObject);
            if (null != response)
                return response;
        } else if (tokenXmlObject instanceof AuthnRequest) {
            RequestSecurityTokenResponseType response = validateSaml2AuthnRequest((AuthnRequest) tokenXmlObject);
            if (null != response)
                return response;
        } else if (tokenXmlObject instanceof LogoutResponse) {
            RequestSecurityTokenResponseType response = validateSaml2LogoutResponse((LogoutResponse) tokenXmlObject);
            if (null != response)
                return response;
        } else if (tokenXmlObject instanceof LogoutRequest) {
            RequestSecurityTokenResponseType response = validateSaml2LogoutRequest((LogoutRequest) tokenXmlObject);
            if (null != response)
                return response;
        } else if (tokenXmlObject instanceof DeviceOperationResponse) {
            RequestSecurityTokenResponseType response = validateDeviceOperationResponse((DeviceOperationResponse) tokenXmlObject);
            if (null != response)
                return response;
        } else if (tokenXmlObject instanceof DeviceOperationRequest) {
            RequestSecurityTokenResponseType response = validateDeviceOperationRequest((DeviceOperationRequest) tokenXmlObject);
            if (null != response)
                return response;
        } else {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "token not a SAML2 Response, AuthnRequest, LogoutResponse, LogoutRequest, "
                            + "DeviceOperationResponse or DeviceOperationRequest");
            return response;

        }

        if (null == tokenType) {
            tokenType = SecurityTokenServiceConstants.TOKEN_TYPE_STATUS;
        }
        RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_VALID, tokenType, null);
        return response;
    }

    private RequestSecurityTokenResponseType validateSaml2AuthnRequest(AuthnRequest samlAuthnRequest) {

        Issuer issuer = samlAuthnRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer name: " + issuerName);

        String assertionConsumerURL = samlAuthnRequest.getAssertionConsumerServiceURL();
        if (null == assertionConsumerURL) {
            LOG.debug("missing assertion consumer URL");
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "missing assertion consumer URL");
            return response;
        }
        LOG.debug("assertionConsumerURL: " + assertionConsumerURL);

        RequestedAuthnContext requestedAuthnContext = samlAuthnRequest.getRequestedAuthnContext();
        if (null == requestedAuthnContext) {
            LOG.debug("missing requested authentication context");
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "missing requested authentication context");
            return response;
        }
        List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        for (AuthnContextClassRef authnContextClassRef : authnContextClassRefs) {
            LOG.debug("requested authentication context: " + authnContextClassRef.getAuthnContextClassRef());
        }

        return null;
    }

    private RequestSecurityTokenResponseType validateSaml2Response(Response samlResponse) {

        String samlStatusCode = samlResponse.getStatus().getStatusCode().getValue();
        if (samlStatusCode.equals(StatusCode.AUTHN_FAILED_URI))
            /**
             * Authentication failed but response was valid.
             */
            return null;
        else if (samlStatusCode.equals(StatusCode.UNKNOWN_PRINCIPAL_URI))
            /**
             * Authentication failed, response valid, user requested to try another device.
             */
            return null;
        else if (false == StatusCode.SUCCESS_URI.equals(samlStatusCode)) {
            LOG.debug("SAML status code: " + samlStatusCode);
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "invalid SAML2 token status code");
            return response;
        }

        List<Assertion> assertions = samlResponse.getAssertions();
        if (assertions.isEmpty()) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "missing Assertion in SAML2 Response");
            return response;
        }
        Assertion assertion = assertions.get(0);

        Conditions conditions = assertion.getConditions();
        DateTime notBefore = conditions.getNotBefore();
        DateTime notOnOrAfter = conditions.getNotOnOrAfter();
        DateTime now = new DateTime();
        if (now.isBefore(notBefore) || now.isAfter(notOnOrAfter)) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "invalid SAML message timeframe");
            return response;
        }

        Subject subject = assertion.getSubject();
        if (null == subject) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "missing Assertion Subject");
            return response;
        }
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
        if (authnStatements.isEmpty()) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "no authentication statement present");
            return response;
        }
        AuthnStatement authnStatement = authnStatements.get(0);
        AuthnContextClassRef authnContextClassRef = authnStatement.getAuthnContext().getAuthnContextClassRef();
        String authnDevice = authnContextClassRef.getAuthnContextClassRef();
        if (null == authnDevice) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "authentication device cannot be null");
            return response;
        }
        LOG.debug("authentication device: " + authnDevice);
        return null;
    }

    private RequestSecurityTokenResponseType validateSaml2LogoutResponse(LogoutResponse samlLogoutResponse) {

        String samlStatusCode = samlLogoutResponse.getStatus().getStatusCode().getValue();
        if (!samlStatusCode.equals(StatusCode.SUCCESS_URI))
            /**
             * Logout failed but response was valid.
             */
            return null;

        Issuer issuer = samlLogoutResponse.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer name: " + issuerName);

        return null;
    }

    private RequestSecurityTokenResponseType validateSaml2LogoutRequest(LogoutRequest samlLogoutRequest) {

        Issuer issuer = samlLogoutRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer name: " + issuerName);

        String subjectName = samlLogoutRequest.getNameID().getValue();
        if (null == subjectName) {
            LOG.debug("missing NameID field");
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID, "missing NameID field");
            return response;
        }
        LOG.debug("subject name: " + subjectName);

        return null;
    }

    private RequestSecurityTokenResponseType validateDeviceOperationRequest(DeviceOperationRequest deviceOperationRequest) {

        Issuer issuer = deviceOperationRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer name: " + issuerName);

        String serviceURL = deviceOperationRequest.getServiceURL();
        if (null == serviceURL) {
            LOG.debug("missing service URL");
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID, "missing service URL");
            return response;
        }
        LOG.debug("serviceURL: " + serviceURL);

        String deviceOperation = deviceOperationRequest.getDeviceOperation();
        if (null == deviceOperation) {
            LOG.debug("missing device operation");
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "missing device operation");
            return response;
        }
        LOG.debug("deviceOperation: " + deviceOperation);

        String device = deviceOperationRequest.getDevice();
        if (null == device) {
            LOG.debug("missing device");
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID, "missing device");
            return response;
        }
        LOG.debug("device: " + device);

        return null;
    }

    private RequestSecurityTokenResponseType validateDeviceOperationResponse(DeviceOperationResponse deviceOperationResponse) {

        String samlStatusCode = deviceOperationResponse.getStatus().getStatusCode().getValue();
        if (samlStatusCode.equals(DeviceOperationResponse.FAILED_URI))
            /**
             * Device operation failed but response was valid.
             */
            return null;
        else if (samlStatusCode.equals(StatusCode.REQUEST_UNSUPPORTED_URI))
            /**
             * Device operation failed, response valid, request was unsupported.
             */
            return null;
        else if (false == StatusCode.SUCCESS_URI.equals(samlStatusCode)) {
            LOG.debug("SAML status code: " + samlStatusCode);
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "invalid SAML2 token status code");
            return response;
        }

        // Device operation does not necessarily have assertions attached
        List<Assertion> assertions = deviceOperationResponse.getAssertions();
        if (assertions.isEmpty())
            return null;

        Assertion assertion = assertions.get(0);

        Conditions conditions = assertion.getConditions();
        DateTime notBefore = conditions.getNotBefore();
        DateTime notOnOrAfter = conditions.getNotOnOrAfter();
        DateTime now = new DateTime();
        if (now.isBefore(notBefore) || now.isAfter(notOnOrAfter)) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "invalid SAML message timeframe");
            return response;
        }

        Subject subject = assertion.getSubject();
        if (null == subject) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "missing Assertion Subject");
            return response;
        }
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
        if (authnStatements.isEmpty()) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "no authentication statement present");
            return response;
        }
        AuthnStatement authnStatement = authnStatements.get(0);
        AuthnContextClassRef authnContextClassRef = authnStatement.getAuthnContext().getAuthnContextClassRef();
        String authnDevice = authnContextClassRef.getAuthnContextClassRef();
        if (null == authnDevice) {
            RequestSecurityTokenResponseType response = createResponse(SecurityTokenServiceConstants.STATUS_INVALID,
                    "authentication device cannot be null");
            return response;
        }
        LOG.debug("authentication device: " + authnDevice);
        return null;
    }

    private RequestSecurityTokenResponseType createResponse(String statusCode, String reason) {

        RequestSecurityTokenResponseType response = createResponse(statusCode, null, reason);
        return response;
    }

    private RequestSecurityTokenResponseType createResponse(String statusCode, String tokenType, String reason) {

        ObjectFactory objectFactory = new ObjectFactory();
        RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
        StatusType status = objectFactory.createStatusType();
        status.setCode(statusCode);
        if (null != reason) {
            status.setReason(reason);
        }
        if (null != tokenType) {
            response.getAny().add(objectFactory.createTokenType(tokenType));
        }
        response.getAny().add(objectFactory.createStatus(status));
        return response;
    }
}
