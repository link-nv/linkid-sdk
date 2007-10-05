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

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingPortImpl;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingServiceFactory;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingRequestType;
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

	private ApplicationAuthenticationService mockAuthenticationService;

	private PkiValidator mockPkiValidator;

	private ConfigurationManager mockConfigurationManager;

	private Object[] mockObjects;

	private X509Certificate certificate;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockAuthenticationService = createMock(ApplicationAuthenticationService.class);
		this.mockPkiValidator = createMock(PkiValidator.class);
		this.mockConfigurationManager = createMock(ConfigurationManager.class);

		this.mockObjects = new Object[] { this.mockAuthenticationService,
				this.mockPkiValidator, this.mockConfigurationManager };

		this.jndiTestUtils.bindComponent(
				"SafeOnline/ApplicationAuthenticationServiceBean/local",
				this.mockAuthenticationService);
		this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local",
				this.mockPkiValidator);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ConfigurationManagerBean/local",
				this.mockConfigurationManager);

		this.webServiceTestUtils = new WebServiceTestUtils();
		NameIdentifierMappingPort wsPort = new NameIdentifierMappingPortImpl();
		this.webServiceTestUtils.setUp(wsPort);

		NameIdentifierMappingService service = NameIdentifierMappingServiceFactory
				.newInstance();
		this.clientPort = service.getNameIdentifierMappingPort();
		this.webServiceTestUtils.setEndpointAddress(this.clientPort);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");

		BindingProvider bindingProvider = (BindingProvider) clientPort;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(
				certificate, keyPair.getPrivate());
		handlerChain.add(wsSecurityHandler);
		binding.setHandlerChain(handlerChain);

		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);
		expect(
				this.mockPkiValidator.validateCertificate((String) EasyMock
						.anyObject(), (X509Certificate) EasyMock.anyObject()))
				.andStubReturn(true);
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

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
		NameIDMappingRequestType request = new NameIDMappingRequestType();

		// prepare
		replay(this.mockObjects);

		// operate
		this.clientPort.nameIdentifierMappingQuery(request);

		// verify
		verify(this.mockObjects);
	}
}
