/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sts.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sts.ws.SecurityTokenServiceFactory;
import net.link.safeonline.sts.ws.SecurityTokenServicePortImpl;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.ws.util.LoggingHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenService;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ValidateTargetType;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.w3c.dom.Element;

public class SecurityTokenServicePortImplTest {

	static final Log LOG = LogFactory
			.getLog(SecurityTokenServicePortImplTest.class);

	private WebServiceTestUtils webServiceTestUtils;

	private JndiTestUtils jndiTestUtils;

	private ApplicationAuthenticationService mockAuthenticationService;

	private PkiValidator mockPkiValidator;

	private ConfigurationManager mockConfigurationManager;

	private Object[] mockObjects;

	PrivateKey privateKey;

	private X509Certificate certificate;

	PublicKey publicKey;

	private JmxTestUtils jmxTestUtils;

	@Before
	public void setUp() throws Exception {
		this.jmxTestUtils = new JmxTestUtils();
		this.jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.privateKey = keyPair.getPrivate();
		this.publicKey = keyPair.getPublic();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=TestApplication");

		this.jmxTestUtils.registerActionHandler("getPrivateKey",
				new MBeanActionHandler() {
					public Object invoke(@SuppressWarnings("unused")
					Object[] arguments) {
						LOG.debug("returning private key");
						return SecurityTokenServicePortImplTest.this.privateKey;
					}
				});
		this.jmxTestUtils.registerActionHandler("getPublicKey",
				new MBeanActionHandler() {
					public Object invoke(@SuppressWarnings("unused")
					Object[] arguments) {
						LOG.debug("returning public key");
						return SecurityTokenServicePortImplTest.this.publicKey;
					}
				});

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

		SecurityTokenServicePort port = new SecurityTokenServicePortImpl();
		this.webServiceTestUtils.setUp(port);

		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);
		expect(
				this.mockPkiValidator.validateCertificate("applications",
						this.certificate)).andStubReturn(true);
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andStubReturn("test-application");

		JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
	}

	@After
	public void tearDown() throws Exception {
		this.webServiceTestUtils.tearDown();
		this.jndiTestUtils.tearDown();
	}

	@BeforeClass
	public static void classSetUp() throws Exception {
		/*
		 * Next is because Sun loves to endorse crippled versions of Xerces.
		 */
		System
				.setProperty(
						"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
						"org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			throw new RuntimeException(
					"could not bootstrap the OpenSAML2 library");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWS() throws Exception {
		// setup
		SecurityTokenService service = SecurityTokenServiceFactory
				.newInstance();
		SecurityTokenServicePort port = service.getSecurityTokenServicePort();
		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				this.webServiceTestUtils.getEndpointAddress());
		List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
		handlers.add(new WSSecurityClientHandler(this.certificate,
				this.privateKey));
		handlers.add(new LoggingHandler());
		handlers.add(new SignatureVerificationTestHandler());

		bindingProvider.getBinding().setHandlerChain(handlers);

		// prepare
		replay(this.mockObjects);

		// operate
		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<String> requestType = objectFactory
				.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate");
		RequestSecurityTokenType request = new RequestSecurityTokenType();
		request.getAny().add(requestType);
		ValidateTargetType validateTarget = new ValidateTargetType();

		Element responseToken = createAuthResponse("test-in-response-to",
				"test-issuer", "test-subject", 60);
		validateTarget.setAny(responseToken);
		request.getAny()
				.add(objectFactory.createValidateTarget(validateTarget));

		RequestSecurityTokenResponseType response = port
				.requestSecurityToken(request);

		// verify
		verify(this.mockObjects);
		assertNotNull(response);
	}

	private Element createAuthResponse(String inResponseTo, String issuerName,
			String subjectName, int validity) {
		Response response = buildXMLObject(Response.class,
				Response.DEFAULT_ELEMENT_NAME);

		DateTime now = new DateTime();

		SecureRandomIdentifierGenerator idGenerator;
		try {
			idGenerator = new SecureRandomIdentifierGenerator();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("secure random init error: "
					+ e.getMessage(), e);
		}
		response.setID(idGenerator.generateIdentifier());
		response.setVersion(SAMLVersion.VERSION_20);
		response.setInResponseTo(inResponseTo);
		response.setIssueInstant(now);

		Issuer responseIssuer = buildXMLObject(Issuer.class,
				Issuer.DEFAULT_ELEMENT_NAME);
		responseIssuer.setValue(issuerName);
		response.setIssuer(responseIssuer);

		Status status = buildXMLObject(Status.class,
				Status.DEFAULT_ELEMENT_NAME);
		StatusCode statusCode = buildXMLObject(StatusCode.class,
				StatusCode.DEFAULT_ELEMENT_NAME);
		statusCode.setValue(StatusCode.SUCCESS_URI);
		status.setStatusCode(statusCode);
		response.setStatus(status);

		Assertion assertion = buildXMLObject(Assertion.class,
				Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setID(idGenerator.generateIdentifier());
		assertion.setIssueInstant(now);
		response.getAssertions().add(assertion);

		Issuer assertionIssuer = buildXMLObject(Issuer.class,
				Issuer.DEFAULT_ELEMENT_NAME);
		assertionIssuer.setValue(issuerName);
		assertion.setIssuer(assertionIssuer);

		Subject subject = buildXMLObject(Subject.class,
				Subject.DEFAULT_ELEMENT_NAME);
		NameID nameID = buildXMLObject(NameID.class,
				NameID.DEFAULT_ELEMENT_NAME);
		nameID.setValue(subjectName);
		subject.setNameID(nameID);
		assertion.setSubject(subject);

		Conditions conditions = buildXMLObject(Conditions.class,
				Conditions.DEFAULT_ELEMENT_NAME);
		conditions.setNotBefore(now);
		conditions.setNotOnOrAfter(now.plusSeconds(validity));
		assertion.setConditions(conditions);

		AuthnStatement authnStatement = buildXMLObject(AuthnStatement.class,
				AuthnStatement.DEFAULT_ELEMENT_NAME);
		assertion.getAuthnStatements().add(authnStatement);
		authnStatement.setAuthnInstant(now);
		AuthnContext authnContext = buildXMLObject(AuthnContext.class,
				AuthnContext.DEFAULT_ELEMENT_NAME);
		authnStatement.setAuthnContext(authnContext);

		AuthnContextClassRef authnContextClassRef = buildXMLObject(
				AuthnContextClassRef.class,
				AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
		authnContext.setAuthnContextClassRef(authnContextClassRef);
		authnContextClassRef
				.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI");

		Signature signature = buildXMLObject(Signature.class,
				Signature.DEFAULT_ELEMENT_NAME);
		signature
				.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		signature
				.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);
		response.setSignature(signature);
		BasicCredential signingCredential = SecurityHelper.getSimpleCredential(
				this.publicKey, this.privateKey);
		signature.setSigningCredential(signingCredential);

		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(response);
		Element responseElement;
		try {
			responseElement = marshaller.marshall(response);
		} catch (MarshallingException e) {
			throw new RuntimeException("opensaml2 marshalling error: "
					+ e.getMessage(), e);
		}

		// sign after marshalling of course
		Signer.signObject(signature);

		return responseElement;
	}

	@SuppressWarnings("unchecked")
	private static <Type extends XMLObject> Type buildXMLObject(
			@SuppressWarnings("unused")
			Class<Type> clazz, QName objectQName) {
		XMLObjectBuilder<Type> builder = Configuration.getBuilderFactory()
				.getBuilder(objectQName);
		if (builder == null) {
			throw new RuntimeException(
					"Unable to retrieve builder for object QName "
							+ objectQName);
		}
		Type object = builder.buildObject(objectQName.getNamespaceURI(),
				objectQName.getLocalPart(), objectQName.getPrefix());
		return object;
	}
}
