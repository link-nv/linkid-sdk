package net.link.safeonline.device.sdk.saml2;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.exception.AuthenticationFinalizationException;
import net.link.safeonline.device.sdk.exception.AuthenticationInitializationException;
import net.link.safeonline.saml2.util.AuthnResponseFactory;
import net.link.safeonline.saml2.util.SamlRequestSecurityPolicyResolver;
import net.link.safeonline.sdk.DomUtils;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Saml2Handler {

	private HttpSession session;

	private String wsLocation;

	private KeyPair applicationKeyPair;

	private X509Certificate applicationCertificate;

	public static final String SAML2_HANDLER = Saml2Handler.class.getName()
			+ ".SAML2_HANDLER";

	public static final String IN_RESPONSE_TO_ATTRIBUTE = Saml2Handler.class
			.getName()
			+ ".IN_RESPONSE_TO";

	public static final String TARGET_URL = Saml2Handler.class.getName()
			+ ".TARGET_URL";

	static {
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

	private Saml2Handler(HttpServletRequest request) {
		this.session = request.getSession();
		this.session.setAttribute(SAML2_HANDLER, this);
	}

	public static Saml2Handler getSaml2Handler(HttpServletRequest request) {
		Saml2Handler instance = (Saml2Handler) request.getSession()
				.getAttribute(SAML2_HANDLER);

		if (null == instance) {
			instance = new Saml2Handler(request);
		}

		return instance;
	}

	public void init(Map<String, String> configParams,
			X509Certificate applicationCertificate, KeyPair applicationKeyPair) {
		this.wsLocation = configParams.get("WsLocation");
		this.applicationCertificate = applicationCertificate;
		this.applicationKeyPair = applicationKeyPair;
	}

	public void initAuthentication(HttpServletRequest request)
			throws AuthenticationInitializationException {

		String encodedSamlRequest = request.getParameter("SAMLRequest");
		if (null == encodedSamlRequest) {
			throw new AuthenticationInitializationException(
					"no SAML request found");
		}

		byte[] decodedSamlResponse;
		try {
			decodedSamlResponse = Base64.decode(encodedSamlRequest);
		} catch (Base64DecodingException e) {
			throw new AuthenticationInitializationException(
					"BASE64 decoding error");
		}
		Document samlDocument;
		try {
			samlDocument = DomUtils.parseDocument(new String(
					decodedSamlResponse));
		} catch (Exception e) {
			throw new AuthenticationInitializationException("DOM parsing error");
		}
		Element samlElement = samlDocument.getDocumentElement();
		SecurityTokenServiceClient stsClient = new SecurityTokenServiceClientImpl(
				this.wsLocation, this.applicationCertificate,
				this.applicationKeyPair.getPrivate());
		try {
			stsClient.validate(samlElement);
		} catch (RuntimeException e) {
			throw new AuthenticationInitializationException(e.getMessage());
		}

		BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
		messageContext
				.setInboundMessageTransport(new HttpServletRequestAdapter(
						request));

		messageContext
				.setSecurityPolicyResolver(new SamlRequestSecurityPolicyResolver());

		HTTPPostDecoder decoder = new HTTPPostDecoder();
		try {
			decoder.decode(messageContext);
		} catch (MessageDecodingException e) {
			throw new AuthenticationInitializationException(
					"SAML message decoding error");
		} catch (SecurityPolicyException e) {
			throw new AuthenticationInitializationException(
					"security policy error");
		} catch (SecurityException e) {
			throw new AuthenticationInitializationException("security error");
		}

		SAMLObject samlMessage = messageContext.getInboundSAMLMessage();
		if (false == samlMessage instanceof AuthnRequest) {
			throw new AuthenticationInitializationException(
					"SAML message not an authentication request message");
		}
		AuthnRequest samlAuthnRequest = (AuthnRequest) samlMessage;

		String assertionConsumerService = samlAuthnRequest
				.getAssertionConsumerServiceURL();

		if (null == assertionConsumerService) {
			throw new AuthenticationInitializationException(
					"missing AssertionConsumerServiceURL");
		}

		String application = null;
		try {
			application = samlAuthnRequest.getConditions()
					.getAudienceRestrictions().get(0).getAudiences().get(0)
					.getAudienceURI();
		} catch (Exception e) {
			// empty
		}

		if (null == application)
			throw new AuthenticationInitializationException(
					"No target application was specified");

		String samlAuthnRequestId = samlAuthnRequest.getID();

		RequestedAuthnContext requestedAuthnContext = samlAuthnRequest
				.getRequestedAuthnContext();
		Set<String> devices;

		if (null != requestedAuthnContext) {
			List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext
					.getAuthnContextClassRefs();
			devices = new HashSet<String>();
			for (AuthnContextClassRef authnContextClassRef : authnContextClassRefs) {
				devices.add(authnContextClassRef.getAuthnContextClassRef());
			}
		} else {
			devices = null;
		}

		HttpSession session = request.getSession();
		session.setAttribute(IN_RESPONSE_TO_ATTRIBUTE, samlAuthnRequestId);
		session.setAttribute(TARGET_URL, assertionConsumerService);

		AuthenticationContext authenticationContext = AuthenticationContext
				.getLoginManager(request);
		authenticationContext.setWantedDevices(devices);
		authenticationContext.setApplication(application);
	}

	@SuppressWarnings("unchecked")
	public void finalizeAuthentication(HttpServletRequest request,
			HttpServletResponse response)
			throws AuthenticationFinalizationException {
		AuthenticationContext authenticationContext = AuthenticationContext
				.getLoginManager(request);
		String usedDevice = authenticationContext.getUsedDevice();
		String userId = authenticationContext.getUserId();
		String applicationId = authenticationContext.getApplication();
		String target = (String) session.getAttribute(TARGET_URL);
		String inResponseTo = (String) session
				.getAttribute(IN_RESPONSE_TO_ATTRIBUTE);

		String issuerName = authenticationContext.getIssuer();
		PrivateKey privateKey = this.applicationKeyPair.getPrivate();
		PublicKey publicKey = this.applicationKeyPair.getPublic();
		int validity = authenticationContext.getValidity();

		BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
		messageContext
				.setOutboundMessageTransport(new HttpServletResponseAdapter(
						response));

		if (null == inResponseTo) {
			throw new AuthenticationFinalizationException(
					"missing IN_RESPONSE_TO session attribute");
		}
		Response responseMessage = AuthnResponseFactory.createAuthResponse(
				inResponseTo, applicationId, issuerName, userId, usedDevice,
				validity, target);
		messageContext.setOutboundSAMLMessage(responseMessage);

		AssertionConsumerService assertionConsumerService = AuthnResponseFactory
				.buildXMLObject(AssertionConsumerService.class,
						AssertionConsumerService.DEFAULT_ELEMENT_NAME);
		assertionConsumerService.setLocation(target);
		messageContext.setPeerEntityEndpoint(assertionConsumerService);

		BasicCredential signingCredential = SecurityHelper.getSimpleCredential(
				publicKey, privateKey);
		messageContext
				.setOutboundSAMLMessageSigningCredential(signingCredential);

		Properties velocityProperties = new Properties();
		velocityProperties.put("resource.loader", "class");
		velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				Log4JLogChute.class.getName());
		velocityProperties.put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER,
				Saml2Handler.class.getName());
		velocityProperties
				.put("class.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		VelocityEngine velocityEngine;
		try {
			velocityEngine = new VelocityEngine(velocityProperties);
			velocityEngine.init();
		} catch (Exception e) {
			throw new AuthenticationFinalizationException(
					"could not initialize velocity engine");
		}

		HTTPPostEncoder postEncoder = new HTTPPostEncoder(velocityEngine,
				"/templates/saml2-post-binding.vm");
		try {
			postEncoder.encode(messageContext);
		} catch (MessageEncodingException e) {
			throw new AuthenticationFinalizationException(
					"message encoding error: " + e.getMessage());
		}

	}

}
