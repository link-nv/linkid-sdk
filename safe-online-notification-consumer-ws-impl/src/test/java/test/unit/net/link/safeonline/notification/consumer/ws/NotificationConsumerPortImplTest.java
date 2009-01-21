/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.notification.consumer.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
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
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.notification.consumer.ws.NotificationConsumerPortImpl;
import net.link.safeonline.notification.consumer.ws.NotificationConsumerServiceFactory;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.ri.InjectionInstanceResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


public class NotificationConsumerPortImplTest {

    private static final Log                                                     LOG = LogFactory
                                                                                                 .getLog(NotificationConsumerPortImplTest.class);

    private WebServiceTestUtils                                                  webServiceTestUtils;

    private NotificationConsumerPort                                             clientPort;

    private JndiTestUtils                                                        jndiTestUtils;

    private X509Certificate                                                      certificate;

    private WSSecurityConfigurationService                                       mockWSSecurityConfigurationService;

    private ApplicationAuthenticationService                                     mockApplicationAuthenticationService;

    private DeviceAuthenticationService                                          mockDeviceAuthenticationService;

    private NodeAuthenticationService                                            mockNodeAuthenticationService;

    private PkiValidator                                                         mockPkiValidator;

    private net.link.safeonline.notification.service.NotificationConsumerService mockNotificationConsumerService;

    private Object[]                                                             mockObjects;


    @SuppressWarnings("unchecked")
    @Before
    public void setup()
            throws Exception {

        LOG.debug("setup");

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", "SafeOnline/WSSecurityConfigurationBean/local");
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);

        mockWSSecurityConfigurationService = createMock(WSSecurityConfigurationService.class);
        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockPkiValidator = createMock(PkiValidator.class);
        mockNotificationConsumerService = createMock(net.link.safeonline.notification.service.NotificationConsumerService.class);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockApplicationAuthenticationService,
                mockDeviceAuthenticationService, mockNodeAuthenticationService, mockPkiValidator, mockNotificationConsumerService };

        jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, mockWSSecurityConfigurationService);
        jndiTestUtils.bindComponent(ApplicationAuthenticationService.JNDI_BINDING, mockApplicationAuthenticationService);
        jndiTestUtils.bindComponent(DeviceAuthenticationService.JNDI_BINDING, mockDeviceAuthenticationService);
        jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, mockNodeAuthenticationService);
        jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, mockPkiValidator);
        jndiTestUtils.bindComponent(net.link.safeonline.notification.service.NotificationConsumerService.JNDI_BINDING,
                mockNotificationConsumerService);

        // expectations
        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        NotificationConsumerPort wsPort = new NotificationConsumerPortImpl();
        webServiceTestUtils = new WebServiceTestUtils();
        webServiceTestUtils.setUp(wsPort);
        /*
         * Next is required, else the wsPort will get old mocks injected when running multiple tests.
         */
        InjectionInstanceResolver.clearInstanceCache();
        NotificationConsumerService service = NotificationConsumerServiceFactory.newInstance();
        clientPort = service.getNotificationConsumerPort();
        webServiceTestUtils.setEndpointAddress(clientPort);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        BindingProvider bindingProvider = (BindingProvider) clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(certificate, keyPair.getPrivate());
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);

    }

    @After
    public void tearDown()
            throws Exception {

        LOG.debug("tearDown");
        webServiceTestUtils.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testNotify()
            throws Exception {

        // setup
        String destination = "test-destination";
        String subject = UUID.randomUUID().toString();
        String content = "test-content";

        NotificationMessageHolderType notificationMessage = new NotificationMessageHolderType();

        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WebServiceConstants.TOPIC_DIALECT_SIMPLE);
        topicExpression.getContent().add(SafeOnlineConstants.TOPIC_REMOVE_USER);
        notificationMessage.setTopic(topicExpression);

        Message message = new Message();
        message.setDestination(destination);
        message.setSubject(subject);
        message.setContent(content);
        notificationMessage.setMessage(message);

        Notify notification = new Notify();
        notification.getNotificationMessage().add(notificationMessage);

        // expectations
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        mockNotificationConsumerService.handleMessage(SafeOnlineConstants.TOPIC_REMOVE_USER, destination, subject, content);

        // prepare
        replay(mockObjects);

        // execute
        clientPort.notify(notification);
    }

}
