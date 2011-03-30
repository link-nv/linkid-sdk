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
import net.lin_k.safe_online.auth.*;
import net.link.safeonline.auth.ws.soap.GetWSAuthenticationServiceFactory;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of get authentication client. This class is using JAX-WS and server-side SSL.
 *
 * @author wvdhaute
 */
public class GetAuthenticationClientImpl extends AbstractWSClient implements GetAuthenticationClient {

    private static final Log LOG = LogFactory.getLog( GetAuthenticationClientImpl.class );

    private final GetWSAuthenticationPort port;

    private final String location;


    /**
     * Main constructor.
     *
     * @param location       the location (host:port/ws-context) of the authentication web service.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public GetAuthenticationClientImpl(String location, X509Certificate sslCertificate) {

        GetWSAuthenticationService getAuthenticationService = GetWSAuthenticationServiceFactory.newInstance();
        port = getAuthenticationService.getGetWSAuthenticationPort();
        this.location = location + "/get_auth";

        setEndpointAddress();

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    public W3CEndpointReference getInstance()
            throws WSClientTransportException {

        LOG.debug( "get instance of stateful authentication service at " + location );

        AuthenticationGetInstanceResponseType response;
        try {
            response = port.getInstance( new AuthenticationGetInstanceRequestType() );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }

        return response.getEndpoint();
    }
}
