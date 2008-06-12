/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthenticatorBaseType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthenticatorTransportProtocolType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthnContextDeclarationBaseType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthnMethodBaseType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.ExtensionOnlyType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.ObjectFactory;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
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
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Factory for SAML2 authentication responses.
 * 
 * @author fcorneli
 * 
 */
public class AuthnResponseFactory {

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

	private AuthnResponseFactory() {
		// empty
	}

	/**
	 * Creates a signed authentication response.
	 */
	public static String createAuthResponse(String inResponseTo,
			String applicationName, String issuerName, String subjectName,
			String samlName, KeyPair signerKeyPair, int validity, String target) {
		if (null == signerKeyPair) {
			throw new IllegalArgumentException(
					"signer key pair should not be null");
		}
		if (null == applicationName) {
			throw new IllegalArgumentException(
					"application name should not be null");
		}
		if (null == issuerName) {
			throw new IllegalArgumentException("issuer name should not be null");
		}
		if (null == subjectName) {
			throw new IllegalArgumentException(
					"subject name should not be null");
		}

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

		response.setDestination(target);

		Status status = buildXMLObject(Status.class,
				Status.DEFAULT_ELEMENT_NAME);
		StatusCode statusCode = buildXMLObject(StatusCode.class,
				StatusCode.DEFAULT_ELEMENT_NAME);
		statusCode.setValue(StatusCode.SUCCESS_URI);
		status.setStatusCode(statusCode);
		response.setStatus(status);

		addAssertion(response, inResponseTo, applicationName, subjectName,
				issuerName, samlName, validity, target);

		return signAuthnResponse(response, signerKeyPair);
	}

	/**
	 * Creates a signed authentication response with status failed.
	 */
	public static String createAuthResponseFailed(String inResponseTo,
			String applicationName, String issuerName, KeyPair signerKeyPair,
			String target) {
		if (null == signerKeyPair) {
			throw new IllegalArgumentException(
					"signer key pair should not be null");
		}
		if (null == applicationName) {
			throw new IllegalArgumentException(
					"application name should not be null");
		}
		if (null == issuerName) {
			throw new IllegalArgumentException("issuer name should not be null");
		}

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

		response.setDestination(target);

		Status status = buildXMLObject(Status.class,
				Status.DEFAULT_ELEMENT_NAME);
		StatusCode statusCode = buildXMLObject(StatusCode.class,
				StatusCode.DEFAULT_ELEMENT_NAME);
		statusCode.setValue(StatusCode.AUTHN_FAILED_URI);
		status.setStatusCode(statusCode);
		response.setStatus(status);

		return signAuthnResponse(response, signerKeyPair);
	}

	/**
	 * Adds an assertion to the unsigned response.
	 * 
	 * @param response
	 * @param subjectName
	 */
	private static void addAssertion(Response response, String inResponseTo,
			String applicationName, String subjectName, String issuerName,
			String samlName, int validity, String target) {

		DateTime now = new DateTime();
		DateTime notAfter = now.plusSeconds(validity);

		SecureRandomIdentifierGenerator idGenerator;
		try {
			idGenerator = new SecureRandomIdentifierGenerator();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("secure random init error: "
					+ e.getMessage(), e);
		}

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
		nameID
				.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		subject.setNameID(nameID);
		assertion.setSubject(subject);

		Conditions conditions = buildXMLObject(Conditions.class,
				Conditions.DEFAULT_ELEMENT_NAME);
		conditions.setNotBefore(now);
		conditions.setNotOnOrAfter(notAfter);
		List<AudienceRestriction> audienceRestrictions = conditions
				.getAudienceRestrictions();
		AudienceRestriction audienceRestriction = buildXMLObject(
				AudienceRestriction.class,
				AudienceRestriction.DEFAULT_ELEMENT_NAME);
		audienceRestrictions.add(audienceRestriction);
		List<Audience> audiences = audienceRestriction.getAudiences();
		Audience audience = buildXMLObject(Audience.class,
				Audience.DEFAULT_ELEMENT_NAME);
		audiences.add(audience);
		audience.setAudienceURI(applicationName);
		assertion.setConditions(conditions);

		List<SubjectConfirmation> subjectConfirmations = subject
				.getSubjectConfirmations();
		SubjectConfirmation subjectConfirmation = buildXMLObject(
				SubjectConfirmation.class,
				SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
		SubjectConfirmationData subjectConfirmationData = buildXMLObject(
				SubjectConfirmationData.class,
				SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		subjectConfirmationData.setRecipient(target);
		subjectConfirmationData.setInResponseTo(inResponseTo);
		subjectConfirmationData.setNotBefore(now);
		subjectConfirmationData.setNotOnOrAfter(notAfter);
		subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
		subjectConfirmations.add(subjectConfirmation);

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
		authnContextClassRef.setAuthnContextClassRef(samlName);
	}

	/**
	 * Sign the unsigned authentication response.
	 */
	private static String signAuthnResponse(Response response,
			KeyPair signerKeyPair) {
		XMLObjectBuilderFactory builderFactory = Configuration
				.getBuilderFactory();
		SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory
				.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
		Signature signature = signatureBuilder.buildObject();
		signature
				.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		String algorithm = signerKeyPair.getPrivate().getAlgorithm();
		if ("RSA".equals(algorithm)) {
			signature
					.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);
		} else if ("DSA".equals(algorithm)) {
			signature
					.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_DSA);
		}
		response.setSignature(signature);
		BasicCredential signingCredential = SecurityHelper.getSimpleCredential(
				signerKeyPair.getPublic(), signerKeyPair.getPrivate());
		signature.setSigningCredential(signingCredential);

		// marshalling
		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(response);
		Element requestElement;
		try {
			requestElement = marshaller.marshall(response);
		} catch (MarshallingException e) {
			throw new RuntimeException("opensaml2 marshalling error: "
					+ e.getMessage(), e);
		}

		// sign after marshaling of course
		try {
			Signer.signObject(signature);
		} catch (SignatureException e) {
			throw new RuntimeException("opensaml2 signing error: "
					+ e.getMessage(), e);
		}

		String result;
		try {
			result = DomUtils.domToString(requestElement);
		} catch (TransformerException e) {
			throw new RuntimeException(
					"DOM to string error: " + e.getMessage(), e);
		}
		return result;
	}

	@SuppressWarnings("unused")
	private static Document createPasswordDeclaration() {
		ObjectFactory objectFactory = new ObjectFactory();
		AuthnContextDeclarationBaseType authnContextDeclaration = objectFactory
				.createAuthnContextDeclarationBaseType();
		AuthnMethodBaseType authnMethod = objectFactory
				.createAuthnMethodBaseType();
		AuthenticatorBaseType authenticator = objectFactory
				.createAuthenticatorBaseType();
		authnMethod.setAuthenticator(authenticator);
		AuthenticatorTransportProtocolType authenticatorTransportProtocol = objectFactory
				.createAuthenticatorTransportProtocolType();
		ExtensionOnlyType ssl = objectFactory.createExtensionOnlyType();
		authenticatorTransportProtocol.setSSL(ssl);
		authnMethod
				.setAuthenticatorTransportProtocol(authenticatorTransportProtocol);
		authnContextDeclaration.setAuthnMethod(authnMethod);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("DOM error");
		}
		Document document = documentBuilder.newDocument();

		try {
			JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			javax.xml.bind.Marshaller marshaller = context.createMarshaller();
			marshaller
					.marshal(
							objectFactory
									.createAuthenticationContextDeclaration(authnContextDeclaration),
							document);
			return document;
		} catch (JAXBException e) {
			throw new RuntimeException("JAXB error: " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <Type extends SAMLObject> Type buildXMLObject(
			@SuppressWarnings("unused") Class<Type> clazz, QName objectQName) {
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
