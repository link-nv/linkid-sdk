/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.notification.producer.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import net.lin_k.safe_online.notification.producer.FilterType;
import net.lin_k.safe_online.notification.producer.NotificationProducerPort;
import net.lin_k.safe_online.notification.producer.NotificationProducerService;
import net.lin_k.safe_online.notification.producer.SubscribeRequest;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.notification.producer.ws.NotificationProducerPortImpl;
import net.link.safeonline.notification.producer.ws.NotificationProducerServiceFactory;
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
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


public class NotificationProducerPortImplTest {

    private static final Log                                                     LOG = LogFactory
                                                                                             .getLog(NotificationProducerPortImplTest.class);

    private WebServiceTestUtils                                                  webServiceTestUtils;

    private NotificationProducerPort                                             clientPort;

    private JndiTestUtils                                                        jndiTestUtils;

    private X509Certificate                                                      certificate;

    private WSSecurityConfigurationService                                       mockWSSecurityConfigurationService;

    private ApplicationAuthenticationService                                     mockApplicationAuthenticationService;

    private DeviceAuthenticationService                                          mockDeviceAuthenticationService;

    private NodeAuthenticationService                                            mockNodeAuthenticationService;

    private net.link.safeonline.notification.service.NotificationProducerService mockNotificationProducerService;

    private PkiValidator                                                         mockPkiValidator;

    private Object[]                                                             mockObjects;


    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {

        LOG.debug("setup");

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");

        this.mockWSSecurityConfigurationService = createMock(WSSecurityConfiguration.class);
        this.mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        this.mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
        this.mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        this.mockPkiValidator = createMock(PkiValidator.class);
        this.mockNotificationProducerService = createMock(net.link.safeonline.notification.service.NotificationProducerService.class);

        this.mockObjects = new Object[] { this.mockWSSecurityConfigurationService,
                this.mockApplicationAuthenticationService, this.mockDeviceAuthenticationService,
                this.mockNodeAuthenticationService, this.mockPkiValidator, this.mockNotificationProducerService };

        this.jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local",
                this.mockWSSecurityConfigurationService);
        this.jndiTestUtils.bindComponent("SafeOnline/ApplicationAuthenticationServiceBean/local",
                this.mockApplicationAuthenticationService);
        this.jndiTestUtils.bindComponent("SafeOnline/DeviceAuthenticationServiceBean/local",
                this.mockDeviceAuthenticationService);
        this.jndiTestUtils.bindComponent("SafeOnline/NodeAuthenticationServiceBean/local",
                this.mockNodeAuthenticationService);
        this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local", this.mockPkiValidator);
        this.jndiTestUtils.bindComponent("SafeOnline/NotificationProducerServiceBean/local",
                this.mockNotificationProducerService);

        // expectations
        expect(
                this.mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock
                        .anyObject())).andStubReturn(PkiResult.VALID);
        expect(this.mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(
                Long.MAX_VALUE);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        NotificationProducerPort wsPort = new NotificationProducerPortImpl();
        this.webServiceTestUtils = new WebServiceTestUtils();
        this.webServiceTestUtils.setUp(wsPort);
        /*
         * Next is required, else the wsPort will get old mocks injected when running multiple tests.
         */
        InjectionInstanceResolver.clearInstanceCache();
        NotificationProducerService service = NotificationProducerServiceFactory.newInstance();
        this.clientPort = service.getNotificationProducerPort();
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
    public void testSubscribe() throws Exception {

        // setup
        String address = "test-consumer";

        SubscribeRequest request = new SubscribeRequest();
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(address);
        W3CEndpointReference consumerEndpoint = builder.build();
        request.setConsumerReference(consumerEndpoint);

        FilterType filter = new FilterType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WebServiceConstants.TOPIC_DIALECT_SIMPLE);
        topicExpression.getContent().add(SafeOnlineConstants.TOPIC_REMOVE_USER);
        filter.setTopic(topicExpression);
        request.setFilter(filter);

        // expectations
        expect(this.mockApplicationAuthenticationService.authenticate(this.certificate)).andReturn(
                "test-application-name");
        expect(this.mockWSSecurityConfigurationService.skipMessageIntegrityCheck(this.certificate)).andReturn(false);
        this.mockNotificationProducerService
                .subscribe(SafeOnlineConstants.TOPIC_REMOVE_USER, address, this.certificate);

        // prepare
        replay(this.mockObjects);

        // execute
        SubscribeResponse response = this.clientPort.subscribe(request);

        // verify
        assertNotNull(response);
    }

}
