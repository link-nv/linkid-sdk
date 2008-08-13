/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ping.ws;

import javax.jws.WebService;

import net.lin_k.safe_online.ping.PingPort;
import net.lin_k.safe_online.ping.Request;
import net.lin_k.safe_online.ping.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@WebService(endpointInterface = "net.lin_k.safe_online.ping.PingPort")
public class PingPortImpl implements PingPort {

    private static final Log LOG = LogFactory.getLog(PingPortImpl.class);


    public Response pingOperation(Request request) {

        LOG.debug("ping");
        Response response = new Response();
        return response;
    }
}
