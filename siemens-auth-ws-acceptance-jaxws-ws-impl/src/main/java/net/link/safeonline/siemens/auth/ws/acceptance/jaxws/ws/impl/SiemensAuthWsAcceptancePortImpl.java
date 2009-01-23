/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.impl;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import net.lin_k.siemens.jaxws.Request;
import net.lin_k.siemens.jaxws.Response;
import net.lin_k.siemens.jaxws.SiemensAuthWsAcceptancePort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of Siemens WS Authentication Acceptance Test WS using JAX-WS.
 * 
 * @author wvdhaute
 * 
 */
@WebService(endpointInterface = "net.lin_k.siemens.jaxws.SiemensAuthWsAcceptancePort")
@HandlerChain(file = "ws-handlers.xml")
public class SiemensAuthWsAcceptancePortImpl implements SiemensAuthWsAcceptancePort {

    private static final Log  LOG = LogFactory.getLog(SiemensAuthWsAcceptancePortImpl.class);

    @Resource
    private WebServiceContext context;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("ready");
    }

    /**
     * {@inheritDoc}
     */
    public Response getAttribute(Request request) {

        Response response = new Response();
        response.setAttribute(new Date().toString());
        return response;
    }
}
