/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws.client;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.AuthenticationService;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.auth.ws.AuthenticationServiceFactory;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * Implementation of device authentication client. This class is using JAX-WS and server-side SSL.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceAuthenticationClientImpl extends AbstractMessageAccessor implements DeviceAuthenticationClient {

    private static final Log           LOG = LogFactory.getLog(DeviceAuthenticationClientImpl.class);

    private final AuthenticationPort   port;

    private final W3CEndpointReference endpoint;


    /**
     * Main constructor
     */
    public DeviceAuthenticationClientImpl(W3CEndpointReference endpoint) {

        this(endpoint, null, null);
    }

    /**
     * Main constructor.
     */
    public DeviceAuthenticationClientImpl(W3CEndpointReference endpoint, X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

        AuthenticationService authenticationService = AuthenticationServiceFactory.newInstance();
        this.endpoint = endpoint;
        port = authenticationService.getPort(endpoint, AuthenticationPort.class, new AddressingFeature(true));

        registerMessageLoggerHandler(port);

        WSSecurityClientHandler.addNewHandler(port, clientCertificate, clientPrivateKey);
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType authenticate(WSAuthenticationRequestType request)
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getAuthenticateResponse(request);

        checkStatus(response);

        return response;
    }

    private WSAuthenticationResponseType getAuthenticateResponse(WSAuthenticationRequestType request)
            throws WSClientTransportException {

        try {
            return port.authenticate(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(endpoint.toString(), e);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(port);
        }
    }

    private void checkStatus(WSAuthenticationResponseType response)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        WSAuthenticationErrorCode wsAuthenticationErrorCode = WSAuthenticationErrorCode.getWSAuthenticationErrorCode(statusCodeValue);
        if (WSAuthenticationErrorCode.SUCCESS != wsAuthenticationErrorCode) {
            LOG.error("status code: " + statusCodeValue);
            LOG.error("status message: " + status.getStatusMessage());
            if (WSAuthenticationErrorCode.REQUEST_DENIED == wsAuthenticationErrorCode)
                throw new RequestDeniedException();
            else if (WSAuthenticationErrorCode.REQUEST_FAILED == wsAuthenticationErrorCode)
                throw new WSClientTransportException(endpoint.toString());
            else
                throw new WSAuthenticationException(wsAuthenticationErrorCode, status.getStatusMessage());
        }
    }
}
