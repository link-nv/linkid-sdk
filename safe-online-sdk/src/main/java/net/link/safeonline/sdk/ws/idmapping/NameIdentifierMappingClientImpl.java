/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.idmapping;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.idmapping.LinkIDNameIDMappingRequestType;
import net.lin_k.safe_online.idmapping.NameIdentifierMappingPort;
import net.link.safeonline.sdk.api.ws.idmapping.NameIdentifierMappingConstants;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingServiceFactory;
import net.link.safeonline.sdk.api.exception.RequestDeniedException;
import net.link.safeonline.sdk.api.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.SamlpSecondLevelErrorCode;
import net.link.safeonline.sdk.api.ws.SamlpTopLevelErrorCode;
import net.link.safeonline.sdk.api.ws.idmapping.client.NameIdentifierMappingClient;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
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
public class NameIdentifierMappingClientImpl extends AbstractWSClient<NameIdentifierMappingPort> implements NameIdentifierMappingClient {

    private static final Log LOG = LogFactory.getLog( NameIdentifierMappingClientImpl.class );

    private final String location;


    /**
     * Main constructor.
     *
     * @param location the location (host:port) of the attribute web service.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     * @param configuration The WS-Security configuration.
     */
    public NameIdentifierMappingClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( NameIdentifierMappingServiceFactory.newInstance().getNameIdentifierMappingPort() );
        getBindingProvider().getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location = location + "/idmapping" );

        registerTrustManager( sslCertificate );
        WSSecurityHandler.install( getBindingProvider(), configuration );
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
            response = getPort().nameIdentifierMappingQuery( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
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


        return response.getNameID().getValue();
    }
}
