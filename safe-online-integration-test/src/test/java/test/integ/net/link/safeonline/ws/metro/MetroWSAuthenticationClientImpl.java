/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws.metro;

import java.net.ConnectException;

import javax.xml.ws.BindingProvider;

import net.lin_k.siemens.metro.Request;
import net.lin_k.siemens.metro.Response;
import net.lin_k.siemens.metro.SiemensWSAuthenticationService;
import net.lin_k.siemens.metro.SiemensWSAuthenticationServicePort;
import net.link.safeonline.siemens.acceptance.ws.auth.SiemensWSAuthenticationServiceFactory;

import com.sun.xml.ws.client.ClientTransportException;


public class MetroWSAuthenticationClientImpl implements MetroWSAuthenticationClient {

    private final SiemensWSAuthenticationServicePort port;


    /**
     * Main constructor.
     * 
     * @param location
     */
    public MetroWSAuthenticationClientImpl(String location) {

        SiemensWSAuthenticationService service = SiemensWSAuthenticationServiceFactory.newInstance();

        port = service.getSiemensWSAuthenticationServicePort();

        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location + "/siemens-ws/siemens_ws_auth");
    }

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
