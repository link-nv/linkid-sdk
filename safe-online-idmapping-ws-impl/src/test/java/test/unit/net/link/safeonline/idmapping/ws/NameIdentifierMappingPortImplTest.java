/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.idmapping.ws;

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

import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.NodeIdentifierMappingService;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingConstants;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingPortImpl;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingServiceFactory;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingRequestType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingResponseType;
import oasis.names.tc.saml._2_0.protocol.NameIDPolicyType;
import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingPort;
import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingService;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class NameIdentifierMappingPortImplTest {

    private WebServiceTestUtils                 webServiceTestUtils;

    private NameIdentifierMappingPort           clientPort;

    private JndiTestUtils                       jndiTestUtils;

    private WSSecurityConfigurationService      mockWSSecurityConfigurationService;

    private ApplicationAuthenticationService    mockApplicationAuthenticationService;

    private DeviceAuthenticationService         mockDeviceAuthenticationService;

    private NodeAuthenticationService           mockNodeAuthenticationService;

    private PkiValidator                        mockPkiValidator;

    private ApplicationIdentifierMappingService mockApplicationIdentifierMappingService;

    private NodeIdentifierMappingService        mockNodeIdentifierMappingService;

    private Object[]                            mockObjects;

    private X509Certificate                     certificate;

    private X509Certificate                     olasCertificate;

    private PrivateKey                          olasPrivateKey;

    private KeyPair                             keyPair;


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
        mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockPkiValidator = createMock(PkiValidator.class);
        mockApplicationIdentifierMappingService = createMock(ApplicationIdentifierMappingService.class);
        mockNodeIdentifierMappingService = createMock(NodeIdentifierMappingService.class);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockApplicationAuthenticationService,
                mockDeviceAuthenticationService, mockPkiValidator, mockApplicationIdentifierMappingService,
                mockNodeIdentifierMappingService };

        jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", mockWSSecurityConfigurationService);
        jndiTestUtils.bindComponent("SafeOnline/ApplicationAuthenticationServiceBean/local", mockApplicationAuthenticationService);
        jndiTestUtils.bindComponent("SafeOnline/DeviceAuthenticationServiceBean/local", mockDeviceAuthenticationService);
        jndiTestUtils.bindComponent("SafeOnline/NodeAuthenticationServiceBean/local", mockNodeAuthenticationService);
        jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local", mockPkiValidator);
        jndiTestUtils.bindComponent("SafeOnline/ApplicationIdentifierMappingServiceBean/local", mockApplicationIdentifierMappingService);
        jndiTestUtils.bindComponent("SafeOnline/NodeIdentifierMappingServiceBean/local", mockNodeIdentifierMappingService);

        webServiceTestUtils = new WebServiceTestUtils();
        NameIdentifierMappingPort wsPort = new NameIdentifierMappingPortImpl();
        webServiceTestUtils.setUp(wsPort);

        NameIdentifierMappingService service = NameIdentifierMappingServiceFactory.newInstance();
        clientPort = service.getNameIdentifierMappingPort();
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

        String testApplicationName = "test-application-name";
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(testApplicationName);
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
    public void invocation()
            throws Exception {

        // setup
        String username = "test-username";
        NameIDMappingRequestType request = new NameIDMappingRequestType();
        NameIDType nameId = new NameIDType();
        nameId.setValue(username);
        NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
        nameIdPolicy.setFormat(NameIdentifierMappingConstants.NAMEID_FORMAT_PERSISTENT);
        request.setNameIDPolicy(nameIdPolicy);
        request.setNameID(nameId);
        String userId = "test-user-id";

        // expectations
        expect(mockApplicationIdentifierMappingService.getApplicationUserId(username)).andReturn(userId);

        // prepare
        replay(mockObjects);

        // operate
        NameIDMappingResponseType response = clientPort.nameIdentifierMappingQuery(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        NameIDType responseNameId = response.getNameID();
        assertNotNull(responseNameId);
        String responseUserId = responseNameId.getValue();
        assertEquals(userId, responseUserId);
    }
}
