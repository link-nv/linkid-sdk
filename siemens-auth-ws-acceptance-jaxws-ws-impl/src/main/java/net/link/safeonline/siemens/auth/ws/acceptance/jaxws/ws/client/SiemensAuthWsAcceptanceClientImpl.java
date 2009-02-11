/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client;

import java.net.ConnectException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.ws.BindingProvider;

import net.lin_k.siemens.jaxws.Request;
import net.lin_k.siemens.jaxws.Response;
import net.lin_k.siemens.jaxws.SiemensAuthWsAcceptancePort;
import net.lin_k.siemens.jaxws.SiemensAuthWsAcceptanceService;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.SiemensAuthWsAcceptanceServiceFactory;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.handler.SamlTokenClientHandler;
import oasis.names.tc.saml._2_0.assertion.AssertionType;

import com.sun.xml.ws.client.ClientTransportException;


public class SiemensAuthWsAcceptanceClientImpl implements SiemensAuthWsAcceptanceClient {

    private final SiemensAuthWsAcceptancePort port;

    private final String                      location;


    public SiemensAuthWsAcceptanceClientImpl(String location, AssertionType assertion, X509Certificate certificate, PrivateKey privateKey) {

        SiemensAuthWsAcceptanceService service = SiemensAuthWsAcceptanceServiceFactory.newInstance();
        port = service.getSiemensAuthWsAcceptancePort();
        this.location = location + "/siemens-ws/test";

        setEndpointAddress();

        SamlTokenClientHandler.addNewHandler(port, assertion, certificate, privateKey);

        // TODO: disable logging when finished
        LoggingHandler.addNewHandler(port);

    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
    }

    /**
     * {@inheritDoc}
     */
    public String getAttribute()
            throws ConnectException {

        Request request = new Request();

        try {
            Response response = port.getAttribute(request);
            return response.getAttribute();
        } catch (ClientTransportException e) {
            throw new ConnectException(e.getMessage());
        }
    }

}
