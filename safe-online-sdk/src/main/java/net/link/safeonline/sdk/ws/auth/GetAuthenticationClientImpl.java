/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import net.lin_k.safe_online.auth.AuthenticationGetInstanceRequestType;
import net.lin_k.safe_online.auth.AuthenticationGetInstanceResponseType;
import net.lin_k.safe_online.auth.GetWSAuthenticationPort;
import net.link.safeonline.auth.ws.soap.GetWSAuthenticationServiceFactory;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.util.ws.AbstractWSClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of get authentication client. This class is using JAX-WS and server-side SSL.
 *
 * @author wvdhaute
 */
public class GetAuthenticationClientImpl extends AbstractWSClient<GetWSAuthenticationPort> implements GetAuthenticationClient {

    private static final Log LOG = LogFactory.getLog( GetAuthenticationClientImpl.class );

    private final String location;


    /**
     * Main constructor.
     *
     * @param location       the location (host:port/ws-context) of the authentication web service.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public GetAuthenticationClientImpl(String location, X509Certificate sslCertificate) {

        super( GetWSAuthenticationServiceFactory.newInstance().getGetWSAuthenticationPort() );
        getBindingProvider().getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location = location + "/get_auth" );

        registerTrustManager( sslCertificate );
    }

    public W3CEndpointReference getInstance()
            throws WSClientTransportException {

        LOG.debug( "get instance of stateful authentication service at " + location );

        AuthenticationGetInstanceResponseType response;
        try {
            response = getPort().getInstance( new AuthenticationGetInstanceRequestType() );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        return response.getEndpoint();
    }
}
