/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.saml2.util;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.w3c.dom.Document;

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
	 * Creates an unsigned authentication response.
	 * 
	 * @param inResponseTo
	 * @param applicationName
	 * @param issuerName
	 * @param subjectName
	 * @param samlAuthnContextClass
	 * @param target
	 */
	public static Response createAuthResponse(String inResponseTo,
			String applicationName, String issuerName, String subjectName,
			String samlName, int validity, String target) {
		Response response = buildXMLObject(Response.class,
				Response.DEFAULT_ELEMENT_NAME);

		DateTime now = new DateTime();
		DateTime notAfter = now.plusSeconds(validity);

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
		nameID
				.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		subject.setNameID(nameID);
		assertion.setSubject(subject);

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

		return response;
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
			Marshaller marshaller = context.createMarshaller();
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
