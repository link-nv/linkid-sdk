/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.session.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.lin_k.safe_online.session.AssertionType;
import net.lin_k.safe_online.session.AuthnStatementType;
import net.lin_k.safe_online.session.SessionTrackingPort;
import net.lin_k.safe_online.session.SessionTrackingRequestType;
import net.lin_k.safe_online.session.SessionTrackingResponseType;
import net.lin_k.safe_online.session.SessionTrackingService;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.NodeIdentifierMappingService;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.session.ws.SessionTrackingPortImpl;
import net.link.safeonline.session.ws.SessionTrackingServiceFactory;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.common.SessionTrackingErrorCode;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SessionTrackingPortImplTest {

    private WebServiceTestUtils                                               webServiceTestUtils;

    private SessionTrackingPort                                               clientPort;

    private JndiTestUtils                                                     jndiTestUtils;

    private WSSecurityConfigurationService                                    mockWSSecurityConfigurationService;
    private ApplicationAuthenticationService                                  mockApplicationAuthenticationService;
    private NodeAuthenticationService                                         mockNodeAuthenticationService;
    private PkiValidator                                                      mockPkiValidator;
    private NodeIdentifierMappingService                                      mockNodeIdentifierMappingService;

    private ApplicationIdentifierMappingService                               mockApplicationIdentifierMappingService;
    private net.link.safeonline.authentication.service.SessionTrackingService mockSessionTrackingService;

    private Object[]                                                          mockObjects;

    private X509Certificate                                                   certificate;

    private X509Certificate                                                   olasCertificate;

    private PrivateKey                                                        olasPrivateKey;

    private KeyPair                                                           keyPair;


    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", "SafeOnline/WSSecurityConfigurationBean/local");
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);

        mockWSSecurityConfigurationService = createMock(WSSecurityConfiguration.class);
        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockPkiValidator = createMock(PkiValidator.class);
        mockApplicationIdentifierMappingService = createMock(ApplicationIdentifierMappingService.class);

        mockNodeIdentifierMappingService = createMock(NodeIdentifierMappingService.class);
        mockSessionTrackingService = createMock(net.link.safeonline.authentication.service.SessionTrackingService.class);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockApplicationAuthenticationService, mockPkiValidator,
                mockApplicationIdentifierMappingService, mockNodeIdentifierMappingService, mockSessionTrackingService };

        jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, mockWSSecurityConfigurationService);
        jndiTestUtils.bindComponent(ApplicationAuthenticationService.JNDI_BINDING, mockApplicationAuthenticationService);
        jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, mockNodeAuthenticationService);
        jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, mockPkiValidator);
        jndiTestUtils.bindComponent(ApplicationIdentifierMappingService.JNDI_BINDING, mockApplicationIdentifierMappingService);
        jndiTestUtils.bindComponent(NodeIdentifierMappingService.JNDI_BINDING, mockNodeIdentifierMappingService);
        jndiTestUtils.bindComponent(net.link.safeonline.authentication.service.SessionTrackingService.JNDI_BINDING,
                mockSessionTrackingService);

        webServiceTestUtils = new WebServiceTestUtils();
        SessionTrackingPort wsPort = new SessionTrackingPortImpl();
        webServiceTestUtils.setUp(wsPort);

        SessionTrackingService service = SessionTrackingServiceFactory.newInstance();
        clientPort = service.getSessionTrackingPort();
        webServiceTestUtils.setEndpointAddress(clientPort);

        keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=OLAS");
        olasPrivateKey = olasKeyPair.getPrivate();

        BindingProvider bindingProvider = (BindingProvider) clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(certificate, keyPair.getPrivate());
        handlerChain.add(wsSecurityHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        long testApplicationId = 1234567890;
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(testApplicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
    }

    @After
    public void tearDown()
            throws Exception {

        webServiceTestUtils.tearDown();

        jndiTestUtils.tearDown();
    }

    @Test
    public void testGetAssertionsNoSubjectNoPools()
            throws Exception {

        // setup
        String session = UUID.randomUUID().toString();
        String ssoId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);
        String applicationUserId = UUID.randomUUID().toString();
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool-name", Long.MAX_VALUE);
        Date authenticationTime = new Date();
        DeviceEntity authenticationDevice = new DeviceEntity();
        authenticationDevice.setName("test-device-name");

        SessionTrackingRequestType request = new SessionTrackingRequestType();
        request.setSession(session);

        List<SessionAssertionEntity> assertions = new LinkedList<SessionAssertionEntity>();
        SessionAssertionEntity assertion = new SessionAssertionEntity(ssoId, applicationPool);
        assertion.setSubject(subject);
        SessionAuthnStatementEntity statement = new SessionAuthnStatementEntity(assertion, authenticationTime, authenticationDevice);
        assertion.setStatements(Collections.singletonList(statement));
        assertions.add(assertion);

        // expectations
        expect(mockSessionTrackingService.getAssertions(session, null, new LinkedList<String>())).andReturn(assertions);
        expect(mockApplicationIdentifierMappingService.getApplicationUserId(subject)).andReturn(applicationUserId);

        // prepare
        replay(mockObjects);

        // operate
        SessionTrackingResponseType response = clientPort.getAssertions(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(SessionTrackingErrorCode.SUCCESS.getErrorCode(), response.getStatus().getValue());
        assertEquals(1, response.getAssertions().size());
        AssertionType assertionType = response.getAssertions().get(0);
        assertEquals(applicationUserId, assertionType.getSubject());
        assertEquals(applicationPool.getName(), assertionType.getApplicationPool());
        assertEquals(1, assertionType.getAuthnStatement().size());
        AuthnStatementType authnStatementType = assertionType.getAuthnStatement().get(0);
        assertEquals(authenticationDevice.getName(), authnStatementType.getDevice());
        XMLGregorianCalendar xmlTime = authnStatementType.getTime();
        assertEquals(authenticationTime, xmlTime.toGregorianCalendar().getTime());
    }
}
