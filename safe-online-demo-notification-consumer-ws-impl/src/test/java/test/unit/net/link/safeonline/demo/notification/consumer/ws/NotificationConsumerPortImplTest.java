/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.demo.notification.consumer.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.lin_k.safe_online.notification.consumer.NotificationConsumerPort;
import net.lin_k.safe_online.notification.consumer.NotificationConsumerService;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType;
import net.lin_k.safe_online.notification.consumer.Notify;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType.Message;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.demo.notification.consumer.ws.NotificationConsumerPortImpl;
import net.link.safeonline.notification.consumer.ws.NotificationConsumerServiceFactory;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.common.WebServiceConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


public class NotificationConsumerPortImplTest {

    private static final Log                                           LOG = LogFactory
                                                                                   .getLog(NotificationConsumerPortImplTest.class);

    private WebServiceTestUtils                                        webServiceTestUtils;

    private NotificationConsumerPort                                   clientPort;

    private JndiTestUtils                                              jndiTestUtils;

    private X509Certificate                                            certificate;

    private WSSecurityConfigurationService                             mockWSSecurityConfigurationService;

    private net.link.safeonline.demo.model.NotificationConsumerService mockNotificationConsumerService;

    private Object[]                                                   mockObjects;


    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {

        LOG.debug("setup");

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnlineDemo/WSSecurityConfigurationBean/local");

        this.mockWSSecurityConfigurationService = createMock(WSSecurityConfigurationService.class);
        this.mockNotificationConsumerService = createMock(net.link.safeonline.demo.model.NotificationConsumerService.class);

        this.mockObjects = new Object[] { this.mockWSSecurityConfigurationService, this.mockNotificationConsumerService };

        this.jndiTestUtils.bindComponent("SafeOnlineDemo/WSSecurityConfigurationBean/local",
                this.mockWSSecurityConfigurationService);
        this.jndiTestUtils.bindComponent("SafeOnlineDemo/NotificationConsumerServiceBean/local",
                this.mockNotificationConsumerService);

        // expectations
        expect(this.mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(
                Long.MAX_VALUE);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        NotificationConsumerPort wsPort = new NotificationConsumerPortImpl();
        this.webServiceTestUtils = new WebServiceTestUtils();
        this.webServiceTestUtils.setUp(wsPort);

        NotificationConsumerService service = NotificationConsumerServiceFactory.newInstance();
        this.clientPort = service.getNotificationConsumerPort();
        this.webServiceTestUtils.setEndpointAddress(this.clientPort);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        BindingProvider bindingProvider = (BindingProvider) this.clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(this.certificate, keyPair
                .getPrivate());
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);

    }

    @After
    public void tearDown() throws Exception {

        LOG.debug("tearDown");
        this.webServiceTestUtils.tearDown();
        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testNotify() throws Exception {

        // setup
        String destination = "test-destination";
        String user = UUID.randomUUID().toString();

        NotificationMessageHolderType notificationMessage = new NotificationMessageHolderType();

        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WebServiceConstants.TOPIC_DIALECT_SIMPLE);
        topicExpression.getContent().add(SafeOnlineConstants.TOPIC_REMOVE_USER);
        notificationMessage.setTopic(topicExpression);

        Message message = new Message();
        message.setDestination(destination);
        List<String> messageContent = new LinkedList<String>();
        messageContent.add(user);
        message.getContent().addAll(messageContent);
        notificationMessage.setMessage(message);

        Notify notification = new Notify();
        notification.getNotificationMessage().add(notificationMessage);

        // expectations
        expect(this.mockWSSecurityConfigurationService.skipMessageIntegrityCheck(this.certificate)).andReturn(false);
        this.mockNotificationConsumerService.handleMessage(SafeOnlineConstants.TOPIC_REMOVE_USER, destination,
                messageContent);

        // prepare
        replay(this.mockObjects);

        // execute
        this.clientPort.notify(notification);
    }
}
