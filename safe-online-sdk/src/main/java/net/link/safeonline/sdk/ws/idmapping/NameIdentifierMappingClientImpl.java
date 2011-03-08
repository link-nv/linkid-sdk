/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.idmapping;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.idmapping.LinkIDNameIDMappingRequestType;
import net.lin_k.safe_online.idmapping.NameIdentifierMappingPort;
import net.lin_k.safe_online.idmapping.NameIdentifierMappingService;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingConstants;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingServiceFactory;
import net.link.safeonline.sdk.ws.SamlpSecondLevelErrorCode;
import net.link.safeonline.sdk.ws.SamlpTopLevelErrorCode;
import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingResponseType;
import oasis.names.tc.saml._2_0.protocol.NameIDPolicyType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the name identifier mapping interface. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class NameIdentifierMappingClientImpl extends AbstractWSClient implements NameIdentifierMappingClient {

    private static final Log LOG = LogFactory.getLog( NameIdentifierMappingClientImpl.class );

    private final NameIdentifierMappingPort port;

    private final String location;


    /**
     * Main constructor.
     *
     * @param location the location (host:port) of the attribute web service.
     * @param clientCertificate the X509 certificate to use for WS-Security signature.
     * @param clientPrivateKey the private key corresponding with the client certificate.
     * @param serverCertificate the X509 certificate of the server
     * @param maxOffset the maximum offset of the WS-Security timestamp received. If <code>null</code> default offset configured in
     *            {@link WSSecurityClientHandler} will be used.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public NameIdentifierMappingClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                                           X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        NameIdentifierMappingService service = NameIdentifierMappingServiceFactory.newInstance();
        port = service.getNameIdentifierMappingPort();
        this.location = location + "/idmapping";
        setEndpointAddress();

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );

        WSSecurityClientHandler.addNewHandler( port, clientCertificate, clientPrivateKey, serverCertificate, maxOffset );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    public String getUserId(String attributeType, String identifier)
            throws SubjectNotFoundException, RequestDeniedException, WSClientTransportException {

        LOG.debug( "getUserId: attributeType=" + attributeType + " identifier=" + identifier );

        LinkIDNameIDMappingRequestType request = new LinkIDNameIDMappingRequestType();
        request.setAttributeType( attributeType );
        NameIDType nameId = new NameIDType();
        nameId.setValue( identifier );
        NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
        nameIdPolicy.setFormat( NameIdentifierMappingConstants.NAMEID_FORMAT_PERSISTENT );
        nameIdPolicy.setAllowCreate( true );
        request.setNameIDPolicy( nameIdPolicy );
        request.setNameID( nameId );

        NameIDMappingResponseType response;
        try {
            response = port.nameIdentifierMappingQuery( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String errorCode = statusCode.getValue();
        SamlpTopLevelErrorCode topLevelErrorCode = SamlpTopLevelErrorCode.getSamlpTopLevelErrorCode( errorCode );
        if (SamlpTopLevelErrorCode.SUCCESS != topLevelErrorCode) {
            // throw new RuntimeException("error occured on identifier mapping
            // service");
            LOG.error( "status code: " + statusCode.getValue() );
            LOG.error( "status message: " + status.getStatusMessage() );
            StatusCodeType secondStatusCode = statusCode.getStatusCode();
            if (null != secondStatusCode) {
                String secondErrorCode = secondStatusCode.getValue();
                SamlpSecondLevelErrorCode secondLevelErrorCode = SamlpSecondLevelErrorCode.getSamlpTopLevelErrorCode( secondErrorCode );
                if (SamlpSecondLevelErrorCode.UNKNOWN_PRINCIPAL == secondLevelErrorCode)
                    throw new SubjectNotFoundException();
                if (SamlpSecondLevelErrorCode.REQUEST_DENIED == secondLevelErrorCode)
                    throw new RequestDeniedException();
                throw new RuntimeException( "error occurred on identifier mapping service: " + secondErrorCode );
            }
        }

        NameIDType responseNameId = response.getNameID();

        String userId = responseNameId.getValue();
        return userId;
    }
}
