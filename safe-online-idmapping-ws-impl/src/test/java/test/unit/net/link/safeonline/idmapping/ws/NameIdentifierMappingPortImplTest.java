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
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.IdentifierMappingService;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingConstants;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingPortImpl;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingServiceFactory;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
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

	private WebServiceTestUtils webServiceTestUtils;

	private NameIdentifierMappingPort clientPort;

	private JndiTestUtils jndiTestUtils;

	private ApplicationAuthenticationService mockApplicationAuthenticationService;

	private DeviceAuthenticationService mockDeviceAuthenticationService;

	private PkiValidator mockPkiValidator;

	private ConfigurationManager mockConfigurationManager;

	private IdentifierMappingService mockIdentifierMappingService;

	private Object[] mockObjects;

	private X509Certificate certificate;

	private KeyPair keyPair;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
		this.mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
		this.mockPkiValidator = createMock(PkiValidator.class);
		this.mockConfigurationManager = createMock(ConfigurationManager.class);
		this.mockIdentifierMappingService = createMock(IdentifierMappingService.class);

		this.mockObjects = new Object[] {
				this.mockApplicationAuthenticationService,
				this.mockDeviceAuthenticationService, this.mockPkiValidator,
				this.mockConfigurationManager,
				this.mockIdentifierMappingService };

		this.jndiTestUtils.bindComponent(
				"SafeOnline/ApplicationAuthenticationServiceBean/local",
				this.mockApplicationAuthenticationService);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/DeviceAuthenticationServiceBean/local",
				this.mockDeviceAuthenticationService);
		this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local",
				this.mockPkiValidator);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ConfigurationManagerBean/local",
				this.mockConfigurationManager);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/IdentifierMappingServiceBean/local",
				this.mockIdentifierMappingService);

		this.webServiceTestUtils = new WebServiceTestUtils();
		NameIdentifierMappingPort wsPort = new NameIdentifierMappingPortImpl();
		this.webServiceTestUtils.setUp(wsPort);

		NameIdentifierMappingService service = NameIdentifierMappingServiceFactory
				.newInstance();
		this.clientPort = service.getNameIdentifierMappingPort();
		this.webServiceTestUtils.setEndpointAddress(this.clientPort);

		this.keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(
				this.keyPair, "CN=Test");

		BindingProvider bindingProvider = (BindingProvider) this.clientPort;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(
				this.certificate, this.keyPair.getPrivate());
		handlerChain.add(wsSecurityHandler);
		LoggingHandler loggingHandler = new LoggingHandler();
		handlerChain.add(loggingHandler);
		binding.setHandlerChain(handlerChain);

		String testApplicationName = "test-application-name";
		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);
		expect(
				this.mockPkiValidator.validateCertificate((String) EasyMock
						.anyObject(), (X509Certificate) EasyMock.anyObject()))
				.andStubReturn(true);
		expect(
				this.mockApplicationAuthenticationService
						.authenticate(this.certificate)).andReturn(
				testApplicationName);
		expect(
				this.mockApplicationAuthenticationService
						.skipMessageIntegrityCheck(testApplicationName))
				.andReturn(false);

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
		nameIdPolicy
				.setFormat(NameIdentifierMappingConstants.NAMEID_FORMAT_PERSISTENT);
		request.setNameIDPolicy(nameIdPolicy);
		request.setNameID(nameId);
		String userId = "test-user-id";

		// expectations
		expect(this.mockIdentifierMappingService.getUserId(username))
				.andReturn(userId);

		// prepare
		replay(this.mockObjects);

		// operate
		NameIDMappingResponseType response = this.clientPort
				.nameIdentifierMappingQuery(request);

		// verify
		verify(this.mockObjects);
		assertNotNull(response);
		NameIDType responseNameId = response.getNameID();
		assertNotNull(responseNameId);
		String responseUserId = responseNameId.getValue();
		assertEquals(userId, responseUserId);
	}
}
