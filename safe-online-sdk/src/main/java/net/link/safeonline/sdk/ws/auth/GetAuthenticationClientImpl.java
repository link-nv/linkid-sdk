/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationGetInstanceRequestType;
import net.lin_k.safe_online.auth.AuthenticationGetInstanceResponseType;
import net.lin_k.safe_online.auth.GetAuthenticationPort;
import net.lin_k.safe_online.auth.GetAuthenticationService;
import net.link.safeonline.auth.ws.GetAuthenticationServiceFactory;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * Implementation of get authentication client. This class is using JAX-WS and server-side SSL.
 * 
 * @author wvdhaute
 * 
 */
public class GetAuthenticationClientImpl extends AbstractMessageAccessor implements GetAuthenticationClient {

    private static final Log            LOG = LogFactory.getLog(GetAuthenticationClientImpl.class);

    private final GetAuthenticationPort port;

    private final String                location;


    /**
     * Main constructor.
     * 
     * @param location
     *            the location (host:port/ws-context) of the authentication web service.
     */
    public GetAuthenticationClientImpl(String location) {

        GetAuthenticationService getAuthenticationService = GetAuthenticationServiceFactory.newInstance();
        this.port = getAuthenticationService.getGetAuthenticationPort();
        this.location = location + "/get_auth";

        setEndpointAddress();

        registerMessageLoggerHandler(this.port);

        // TODO: disable logging when finished
        LoggingHandler.addNewHandler(this.port);
        setCaptureMessages(true);
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) this.port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location);
    }

    public W3CEndpointReference getInstance()
            throws WSClientTransportException {

        LOG.debug("get instance of stateful authentication service at " + this.location);

        SafeOnlineTrustManager.configureSsl();

        AuthenticationGetInstanceResponseType response;
        try {
            response = this.port.getInstance(new AuthenticationGetInstanceRequestType());
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.location);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }

        return response.getEndpoint();

    }
}
