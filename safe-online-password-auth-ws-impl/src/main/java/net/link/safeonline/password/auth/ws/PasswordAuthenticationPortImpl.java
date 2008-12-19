/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.auth.ws;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.soap.Addressing;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;


/**
 * Implementation of OLAS Stateful Password Authentication web service using JAX-WS.
 * 
 * 
 * Do NOT use {@link Injection} as this is a {@link Stateful} web service and the statefulness is achieved by JAX-WS using the same
 * {@link InstanceResolver} as is used by the {@link Injection}.
 * 
 * @author wvdhaute
 * 
 */

@Stateful
@Addressing
@WebService(endpointInterface = "net.lin_k.safe_online.auth.AuthenticationPort")
// @HandlerChain(file = "auth-ws-handlers.xml")
public class PasswordAuthenticationPortImpl implements AuthenticationPort {

    private static final Log                                    LOG = LogFactory.getLog(PasswordAuthenticationPortImpl.class);

    public static StatefulWebServiceManager<AuthenticationPort> manager;

    private DatatypeFactory                                     datatypeFactory;


    @PostConstruct
    public void postConstructCallback() {

        try {
            this.datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new EJBException("datatype config error");
        }

        LOG.debug("ready");
    }

    public PasswordAuthenticationPortImpl() {

        // XXX: make this configurable ..., time is in ms
        manager.setTimeout(1000 * 60 * 30, new TimeoutCallback());
    }


    class TimeoutCallback implements StatefulWebServiceManager.Callback<AuthenticationPort> {

        /**
         * {@inheritDoc}
         */
        public void onTimeout(AuthenticationPort timedOutObject, StatefulWebServiceManager<AuthenticationPort> manager) {

            // XXX: notify stateful device ws to timeout ?
        }

    }


    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType authenticate(WSAuthenticationRequestType request) {

        LOG.debug("authenticate");

        WSAuthenticationResponseType response = new WSAuthenticationResponseType();

        manager.unexport(this);

        return response;
    }
}
