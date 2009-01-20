/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationGetInstanceRequestType;
import net.lin_k.safe_online.auth.AuthenticationGetInstanceResponseType;
import net.lin_k.safe_online.auth.GetAuthenticationPort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Web service that creates an instance of the stateful authentication web service and returns a {@link W3CEndpointReference} to this
 * instance.
 * 
 * @author wvdhaute
 * 
 */
@WebService(endpointInterface = "net.lin_k.safe_online.auth.GetAuthenticationPort")
public class GetAuthenticationPortImpl implements GetAuthenticationPort {

    private static final Log LOG = LogFactory.getLog(GetAuthenticationPortImpl.class);


    @WebMethod
    public synchronized AuthenticationGetInstanceResponseType getInstance(AuthenticationGetInstanceRequestType request) {

        LOG.debug("return instance of stateful authentication webservice");
        AuthenticationGetInstanceResponseType response = new AuthenticationGetInstanceResponseType();
        response.setEndpoint(AuthenticationPortImpl.manager.export(new AuthenticationPortImpl()));
        return response;

    }
}
