/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.sts;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.safeonline.sdk.ws.WebServiceConstants;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.sts.ws.SecurityTokenServiceFactory;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.*;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.w3c.dom.Element;


/**
 * Implementation of Security Token Service Client.
 *
 * @author fcorneli
 */
public class SecurityTokenServiceClientImpl extends AbstractWSClient implements SecurityTokenServiceClient {

    private static final Log LOG = LogFactory.getLog( SecurityTokenServiceClientImpl.class );

    private final SecurityTokenServicePort port;

    private final String location;

    /**
     * Main constructor.
     *
     * @param location          the location (host:port) of the LinkID STS web service.
     * @param clientCertificate the X509 certificate to use for a WS-Security signature on the STS validation request.
     * @param clientPrivateKey  the private key corresponding with the client certificate.
     * @param serverCertificate optional X509 certificate of the LinkID server, if specified the certificate in the WS-Security header on
     *                          the STS validation response will be checked against this certificate.
     * @param maxOffset         the maximum offset of the WS-Security timestamp received. If <code>null</code> default offset configured in
     *                          {@link WSSecurityClientHandler} will be used.
     * @param sslCertificate    If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public SecurityTokenServiceClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                                          X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();
        port = service.getSecurityTokenServicePort();
        this.location = location + "/sts";
        setEndpointAddress();

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );

        WSSecurityClientHandler.addNewHandler( port, clientCertificate, clientPrivateKey, serverCertificate, maxOffset );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    private void validate(Element token, TrustDomainType trustDomain, Map<QName, String> otherAttributes, String queryString,
                          StringBuffer requestUrl)
            throws WSClientTransportException, ValidationFailedException {

        LOG.debug( "invoke" );
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType(
                WebServiceConstants.WS_TRUST_REQUEST_TYPE + "Validate#" + trustDomain );
        request.getAny().add( requestType );

        JAXBElement<String> tokenType = objectFactory.createTokenType( SecurityTokenServiceConstants.TOKEN_TYPE_STATUS );
        request.getAny().add( tokenType );

        ValidateTargetType validateTarget = new ValidateTargetType();
        validateTarget.setAny( token );
        request.getAny().add( objectFactory.createValidateTarget( validateTarget ) );

        request.getOtherAttributes().put( WebServiceConstants.SAML_QUERY_STRING_ATTRIBUTE, queryString );
        request.getOtherAttributes().put( WebServiceConstants.SAML_REQUEST_URL_ATTRIBUTE, requestUrl.toString() );
        if (null != otherAttributes)
            request.getOtherAttributes().putAll( otherAttributes );

        RequestSecurityTokenResponseType response;
        try {
            response = port.requestSecurityToken( request );
        } catch (Exception e) {
            LOG.error( "Exception: " + e.getMessage(), e );
            throw new WSClientTransportException( location, e );
        } finally {
            LOG.debug( "finally" );
            retrieveHeadersFromPort( port );
            LOG.debug( "finally done" );
        }

        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results)
            if (result instanceof JAXBElement<?>) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType)
                    status = (StatusType) value;
            }
        if (null == status)
            throw new ValidationFailedException( "no Status found in response" );
        String statusCode = status.getCode();
        if (SecurityTokenServiceConstants.STATUS_VALID.equals( statusCode ))
            return;
        String reason = status.getReason();
        LOG.debug( "reason: " + reason );
        throw new ValidationFailedException( "token found to be invalid: " + reason );
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Response response, final String requestIssuer, final HttpServletRequest request)
            throws WSClientTransportException, ValidationFailedException {

        Map<QName, String> otherAttributes = new HashMap<QName, String>();
        otherAttributes.put( WebServiceConstants.SAML_AUDIENCE_ATTRIBUTE, requestIssuer );

        validate( response.getDOM(), TrustDomainType.LINK_ID, otherAttributes, request.getQueryString(), request.getRequestURL() );
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final LogoutResponse logoutResponse, final HttpServletRequest request)
            throws WSClientTransportException, ValidationFailedException {

        validate( logoutResponse.getDOM(), TrustDomainType.LINK_ID, null, request.getQueryString(), request.getRequestURL() );
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final LogoutRequest logoutRequest, final HttpServletRequest request)
            throws WSClientTransportException, ValidationFailedException {

        validate( logoutRequest.getDOM(), TrustDomainType.LINK_ID, null, request.getQueryString(), request.getRequestURL() );
    }
}
