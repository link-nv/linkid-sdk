/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.metro.wsclient;

import java.net.ConnectException;

import javax.xml.ws.BindingProvider;

import net.lin_k.siemens.metro.MetroPort;
import net.lin_k.siemens.metro.MetroService;
import net.lin_k.siemens.metro.Request;
import net.lin_k.siemens.metro.Response;
import net.link.safeonline.siemens.metro.ws.SiemensMetroServiceFactory;

import com.sun.xml.ws.client.ClientTransportException;


public class MetroClientImpl implements MetroClient {

    private final MetroPort metroPort;


    /**
     * Main constructor.
     * 
     * @param location
     */
    public MetroClientImpl(String location) {

        MetroService metroService = SiemensMetroServiceFactory.newInstance();
        metroPort = metroService.getMetroPort();

        BindingProvider bindingProvider = (BindingProvider) metroPort;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location + "/siemens-ws/test");
    }

    public String getAttribute()
            throws ConnectException {

        Request request = new Request();
        try {
            Response response = metroPort.getAttribute(request);
            return response.getAttribute();
        } catch (ClientTransportException e) {
            throw new ConnectException(e.getMessage());
        }
    }
}
