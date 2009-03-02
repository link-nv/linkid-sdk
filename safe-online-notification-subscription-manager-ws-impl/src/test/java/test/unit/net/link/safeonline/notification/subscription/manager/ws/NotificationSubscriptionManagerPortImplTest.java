/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.notification.subscription.manager.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerPort;
import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerService;
import net.lin_k.safe_online.notification.subscription.manager.TopicType;
import net.lin_k.safe_online.notification.subscription.manager.UnsubscribeRequest;
import net.lin_k.safe_online.notification.subscription.manager.UnsubscribeResponse;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.notification.service.NotificationProducerService;
import net.link.safeonline.notification.subscription.manager.ws.NotificationSubscriptionManagerPortImpl;
import net.link.safeonline.notification.subscription.manager.ws.NotificationSubscriptionManagerServiceFactory;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.common.NotificationErrorCode;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.ri.InjectionInstanceResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


public class NotificationSubscriptionManagerPortImplTest {

    private static final Log                    LOG = LogFactory.getLog(NotificationSubscriptionManagerPortImplTest.class);

    private WebServiceTestUtils                 webServiceTestUtils;

    private NotificationSubscriptionManagerPort clientPort;

    private JndiTestUtils                       jndiTestUtils;

    private X509Certificate                     certificate;

    private X509Certificate                     olasCertificate;

    private PrivateKey                          olasPrivateKey;

    private WSSecurityConfiguration             mockWSSecurityConfigurationService;

    private ApplicationAuthenticationService    mockApplicationAuthenticationService;

    private NodeAuthenticationService           mockNodeAuthenticationService;

    private PkiValidator                        mockPkiValidator;

    private NotificationProducerService         mockNotificationProducerService;

    private Object[]                            mockObjects;


    @SuppressWarnings("unchecked")
    @Before
    public void setup()
            throws Exception {

        LOG.debug("setup");

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", "SafeOnline/WSSecurityConfigurationBean/local");
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);

        mockWSSecurityConfigurationService = createMock(WSSecurityConfiguration.class);
        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockPkiValidator = createMock(PkiValidator.class);
        mockNotificationProducerService = createMock(NotificationProducerService.class);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockApplicationAuthenticationService,
                mockNodeAuthenticationService, mockPkiValidator, mockNotificationProducerService };

        jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, mockWSSecurityConfigurationService);
        jndiTestUtils.bindComponent(ApplicationAuthenticationService.JNDI_BINDING, mockApplicationAuthenticationService);
        jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, mockNodeAuthenticationService);
        jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, mockPkiValidator);
        jndiTestUtils.bindComponent(NotificationProducerService.JNDI_BINDING, mockNotificationProducerService);

        // expectations
        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        NotificationSubscriptionManagerPort wsPort = new NotificationSubscriptionManagerPortImpl();
        webServiceTestUtils = new WebServiceTestUtils();
        webServiceTestUtils.setUp(wsPort);
        /*
         * Next is required, else the wsPort will get old mocks injected when running multiple tests.
         */
        InjectionInstanceResolver.clearInstanceCache();
        NotificationSubscriptionManagerService service = NotificationSubscriptionManagerServiceFactory.newInstance();
        clientPort = service.getNotificationSubscriptionManagerPort();
        webServiceTestUtils.setEndpointAddress(clientPort);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=OLAS");
        olasPrivateKey = olasKeyPair.getPrivate();

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
    public void testUnsubscribe()
            throws Exception {

        // setup
        String address = "test-consumer-address";

        UnsubscribeRequest request = new UnsubscribeRequest();

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(address);
        W3CEndpointReference endpoint = builder.build();
        request.setConsumerReference(endpoint);

        TopicType topic = new TopicType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WebServiceConstants.TOPIC_DIALECT_SIMPLE);
        topicExpression.getContent().add(SafeOnlineConstants.TOPIC_REMOVE_USER);
        topic.setTopic(topicExpression);
        request.setTopic(topic);

        // expectations
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(1234567890L);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);
        mockNotificationProducerService.unsubscribe(SafeOnlineConstants.TOPIC_REMOVE_USER, address, certificate);

        // prepare
        replay(mockObjects);

        // execute
        UnsubscribeResponse response = clientPort.unsubscribe(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);

        assertEquals(NotificationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

    }
}
