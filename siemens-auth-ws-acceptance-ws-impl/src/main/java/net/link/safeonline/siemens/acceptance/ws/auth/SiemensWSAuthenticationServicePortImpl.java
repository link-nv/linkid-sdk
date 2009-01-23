/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth;

import java.util.Date;

import javax.jws.WebService;

import net.lin_k.siemens.metro.Request;
import net.lin_k.siemens.metro.Response;
import net.lin_k.siemens.metro.SiemensWSAuthenticationServicePort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@WebService(endpointInterface = "net.lin_k.siemens.metro.SiemensWSAuthenticationServicePort")
public class SiemensWSAuthenticationServicePortImpl implements SiemensWSAuthenticationServicePort {

    private static final Log LOG = LogFactory.getLog(SiemensWSAuthenticationServicePortImpl.class);


    public Response getAttribute(Request request) {

        LOG.debug("get attribute");
        Response response = new Response();
        response.setAttribute(new Date().toString());
        return response;
    }
}
