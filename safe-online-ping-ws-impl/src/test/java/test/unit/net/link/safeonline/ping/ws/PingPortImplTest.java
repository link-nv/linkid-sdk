/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ping.ws;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import net.lin_k.safe_online.ping.PingPort;
import net.lin_k.safe_online.ping.PingService;
import net.lin_k.safe_online.ping.Request;
import net.lin_k.safe_online.ping.Response;
import net.link.safeonline.ping.ws.PingPortImpl;
import net.link.safeonline.ping.ws.PingServiceFactory;
import net.link.safeonline.sdk.ws.MessageLoggerHandler;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PingPortImplTest {

    private static final Log    LOG = LogFactory.getLog(PingPortImplTest.class);

    private WebServiceTestUtils webServiceTestUtils;


    @Before
    public void setUp() throws Exception {

        this.webServiceTestUtils = new WebServiceTestUtils();
        PingPortImpl port = new PingPortImpl();
        this.webServiceTestUtils.setUp(port);
    }

    @After
    public void tearDown() throws Exception {

        this.webServiceTestUtils.tearDown();
    }

    @Test
    public void testInvocation() throws Exception {

        // setup
        PingService service = PingServiceFactory.newInstance();
        PingPort port = service.getPingPort();
        setEndpointAddress(port);
        MessageLoggerHandler logger = installLogger(port);

        // operate
        Request request = new Request();
        Response response;
        try {
            response = port.pingOperation(request);
        } finally {
            LOG.debug("outbound message: " + DomTestUtils.domToString(logger.getOutboundMessage()));
            LOG.debug("inbound message: " + DomTestUtils.domToString(logger.getInboundMessage()));
        }

        // verify
        assertNotNull(response);
    }

    private void setEndpointAddress(PingPort port) {

        BindingProvider bindingProvider = (BindingProvider) port;
        String location = this.webServiceTestUtils.getEndpointAddress();
        LOG.debug("location: " + location);
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
    }

    private MessageLoggerHandler installLogger(PingPort port) {

        BindingProvider bindingProvider = (BindingProvider) port;
        MessageLoggerHandler logger = new MessageLoggerHandler();
        logger.setCaptureMessages(true);
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add(logger);
        binding.setHandlerChain(handlerChain);
        return logger;
    }
}
