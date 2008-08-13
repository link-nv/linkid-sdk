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
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.DeviceIdentifierMappingService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingConstants;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingPortImpl;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingServiceFactory;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.util.LoggingHandler;
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

    private DeviceIdentifierMappingService      mockDeviceIdentifierMappingService;

    private Object[]                            mockObjects;

    private X509Certificate                     certificate;

    private KeyPair                             keyPair;


    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");

        this.mockWSSecurityConfigurationService = createMock(WSSecurityConfiguration.class);
        this.mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        this.mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
        this.mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        this.mockPkiValidator = createMock(PkiValidator.class);
        this.mockApplicationIdentifierMappingService = createMock(ApplicationIdentifierMappingService.class);
        this.mockDeviceIdentifierMappingService = createMock(DeviceIdentifierMappingService.class);

        this.mockObjects = new Object[] { this.mockWSSecurityConfigurationService,
                this.mockApplicationAuthenticationService, this.mockDeviceAuthenticationService, this.mockPkiValidator,
                this.mockApplicationIdentifierMappingService, this.mockDeviceIdentifierMappingService };

        this.jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local",
                this.mockWSSecurityConfigurationService);
        this.jndiTestUtils.bindComponent("SafeOnline/ApplicationAuthenticationServiceBean/local",
                this.mockApplicationAuthenticationService);
        this.jndiTestUtils.bindComponent("SafeOnline/DeviceAuthenticationServiceBean/local",
                this.mockDeviceAuthenticationService);
        this.jndiTestUtils.bindComponent("SafeOnline/NodeAuthenticationServiceBean/local",
                this.mockNodeAuthenticationService);
        this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local", this.mockPkiValidator);
        this.jndiTestUtils.bindComponent("SafeOnline/ApplicationIdentifierMappingServiceBean/local",
                this.mockApplicationIdentifierMappingService);
        this.jndiTestUtils.bindComponent("SafeOnline/DeviceIdentifierMappingServiceBean/local",
                this.mockDeviceIdentifierMappingService);

        this.webServiceTestUtils = new WebServiceTestUtils();
        NameIdentifierMappingPort wsPort = new NameIdentifierMappingPortImpl();
        this.webServiceTestUtils.setUp(wsPort);

        NameIdentifierMappingService service = NameIdentifierMappingServiceFactory.newInstance();
        this.clientPort = service.getNameIdentifierMappingPort();
        this.webServiceTestUtils.setEndpointAddress(this.clientPort);

        this.keyPair = PkiTestUtils.generateKeyPair();
        this.certificate = PkiTestUtils.generateSelfSignedCertificate(this.keyPair, "CN=Test");

        BindingProvider bindingProvider = (BindingProvider) this.clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(this.certificate, this.keyPair
                .getPrivate());
        handlerChain.add(wsSecurityHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        String testApplicationName = "test-application-name";
        expect(this.mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(
                Long.MAX_VALUE);
        expect(
                this.mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock
                        .anyObject())).andStubReturn(PkiResult.VALID);
        expect(this.mockApplicationAuthenticationService.authenticate(this.certificate)).andReturn(testApplicationName);
        expect(this.mockWSSecurityConfigurationService.skipMessageIntegrityCheck(this.certificate)).andReturn(false);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
    }

    @After
    public void tearDown() throws Exception {

        this.webServiceTestUtils.tearDown();

        this.jndiTestUtils.tearDown();
    }

    @Test
    public void invocation() throws Exception {

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
        expect(this.mockApplicationIdentifierMappingService.getApplicationUserId(username)).andReturn(userId);

        // prepare
        replay(this.mockObjects);

        // operate
        NameIDMappingResponseType response = this.clientPort.nameIdentifierMappingQuery(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        NameIDType responseNameId = response.getNameID();
        assertNotNull(responseNameId);
        String responseUserId = responseNameId.getValue();
        assertEquals(userId, responseUserId);
    }
}
