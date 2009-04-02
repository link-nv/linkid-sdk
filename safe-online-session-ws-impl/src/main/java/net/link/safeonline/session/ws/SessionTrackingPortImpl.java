/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.session.ws;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import net.lin_k.safe_online.session.ApplicationPoolType;
import net.lin_k.safe_online.session.SessionTrackingPort;
import net.lin_k.safe_online.session.SessionTrackingRequestType;
import net.lin_k.safe_online.session.SessionTrackingResponseType;
import net.link.safeonline.authentication.service.SessionTrackingService;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of OLAS Session Tracking web service using JAX-WS.
 * 
 * @author wvdhaute
 * 
 */
@WebService(endpointInterface = "net.lin_k.safe_online.session.SessionTrackingPort")
@HandlerChain(file = "auth-ws-handlers.xml")
@Injection
public class SessionTrackingPortImpl implements SessionTrackingPort {

    private static final Log  LOG = LogFactory.getLog(SessionTrackingPortImpl.class);

    @Resource
    private WebServiceContext context;

    @EJB(mappedName = SessionTrackingService.JNDI_BINDING)
    SessionTrackingService    sessionTrackingService;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("ready");
    }

    /**
     * {@inheritDoc}
     */
    public SessionTrackingResponseType getAssertions(SessionTrackingRequestType request) {

        LOG.debug("getAssertions");

        String session = request.getSession();

        String subject = request.getSubject();

        List<String> applicationPools = new LinkedList<String>();
        for (ApplicationPoolType applicationPool : request.getApplicationPools()) {
            applicationPools.add(applicationPool.getName());
        }

        LOG.debug("get assertions for session=" + session + " subject=" + subject);
        sessionTrackingService.getAssertions(session, subject, applicationPools);

        return null;
    }
}
