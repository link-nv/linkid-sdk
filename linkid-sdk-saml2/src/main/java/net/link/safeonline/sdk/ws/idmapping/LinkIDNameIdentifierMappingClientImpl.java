/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.idmapping;

import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.idmapping.LinkIDNameIDMappingRequestType;
import net.lin_k.safe_online.idmapping.NameIdentifierMappingPort;
import net.link.safeonline.sdk.api.exception.LinkIDRequestDeniedException;
import net.link.safeonline.sdk.api.exception.LinkIDSubjectNotFoundException;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.ws.LinkIDSamlpSecondLevelErrorCode;
import net.link.safeonline.sdk.api.ws.LinkIDSamlpTopLevelErrorCode;
import net.link.safeonline.sdk.api.ws.idmapping.LinkIDNameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.idmapping.LinkIDNameIdentifierMappingConstants;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.idmapping.LinkIDNameIdentifierMappingServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.logging.Logger;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingResponseType;
import oasis.names.tc.saml._2_0.protocol.NameIDPolicyType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;


/**
 * Implementation of the name identifier mapping interface. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class LinkIDNameIdentifierMappingClientImpl extends AbstractWSClient<NameIdentifierMappingPort> implements LinkIDNameIdentifierMappingClient {

    private static final Logger logger = Logger.get( LinkIDNameIdentifierMappingClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDNameIdentifierMappingClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDNameIdentifierMappingClientImpl(final String location, final X509Certificate[] sslCertificates,
                                                 final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDNameIdentifierMappingClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDNameIdentifierMappingServiceFactory.newInstance().getNameIdentifierMappingPort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.idmapping.path" ) ) );
    }

    @Override
    public String getUserId(String attributeType, String identifier)
            throws LinkIDSubjectNotFoundException, LinkIDRequestDeniedException, LinkIDWSClientTransportException {

        LinkIDNameIDMappingRequestType request = new LinkIDNameIDMappingRequestType();
        request.setAttributeType( attributeType );
        NameIDType nameId = new NameIDType();
        nameId.setValue( identifier );
        NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
        nameIdPolicy.setFormat( LinkIDNameIdentifierMappingConstants.NAMEID_FORMAT_PERSISTENT );
        nameIdPolicy.setAllowCreate( true );
        request.setNameIDPolicy( nameIdPolicy );
        request.setNameID( nameId );

        NameIDMappingResponseType response;
        try {
            response = getPort().nameIdentifierMappingQuery( request );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String errorCode = statusCode.getValue();
        LinkIDSamlpTopLevelErrorCode topLevelErrorCode = LinkIDSamlpTopLevelErrorCode.getSamlpTopLevelErrorCode( errorCode );
        if (LinkIDSamlpTopLevelErrorCode.SUCCESS != topLevelErrorCode) {
            // throw new RuntimeException("error occured on identifier mapping
            // service");
            logger.err( "status code: %s", statusCode.getValue() );
            logger.err( "status message: %s", status.getStatusMessage() );
            StatusCodeType secondStatusCode = statusCode.getStatusCode();
            if (null != secondStatusCode) {
                String secondErrorCode = secondStatusCode.getValue();
                LinkIDSamlpSecondLevelErrorCode secondLevelErrorCode = LinkIDSamlpSecondLevelErrorCode.getSamlpTopLevelErrorCode( secondErrorCode );
                if (LinkIDSamlpSecondLevelErrorCode.UNKNOWN_PRINCIPAL == secondLevelErrorCode) {
                    throw new LinkIDSubjectNotFoundException();
                }
                if (LinkIDSamlpSecondLevelErrorCode.REQUEST_DENIED == secondLevelErrorCode) {
                    throw new LinkIDRequestDeniedException();
                }
                throw new InternalInconsistencyException( String.format( "Error occurred on identifier mapping service: %s", secondErrorCode ) );
            }
        }

        return response.getNameID().getValue();
    }
}
