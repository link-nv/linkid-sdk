/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import net.lin_k.safe_online.auth.*;
import net.link.safeonline.sdk.SDKUtils;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.auth.client.AuthenticationClientFactory;
import net.link.safeonline.ws.auth.GetWSAuthenticationServiceFactory;
import net.link.util.ws.AbstractWSClient;


public class AuthenticationClientFactoryImpl extends AbstractWSClient<GetWSAuthenticationPort> implements AuthenticationClientFactory {

    private static final Logger logger = Logger.get( AuthenticationClientFactoryImpl.class );

    /**
     * Main constructor.
     *
     * @param location       the location (host:port/ws-context) of the authentication web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public AuthenticationClientFactoryImpl(String location, X509Certificate sslCertificate) {

        super( GetWSAuthenticationServiceFactory.newInstance().getGetWSAuthenticationPort(), sslCertificate );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.get.auth.path" ) ) );
    }

    @Override
    public W3CEndpointReference getInstance()
            throws WSClientTransportException {

        logger.dbg( "get instance of stateful authentication service" );

        AuthenticationGetInstanceResponseType response;
        try {
            response = getPort().getInstance( new AuthenticationGetInstanceRequestType() );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        return response.getEndpoint();
    }
}
