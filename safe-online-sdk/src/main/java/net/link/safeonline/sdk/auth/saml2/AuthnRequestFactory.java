/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import net.link.safeonline.sdk.DomUtils;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
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
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.w3c.dom.Element;

/**
 * Factory class for SAML2 authentication requests.
 * 
 * <p>
 * We're using the OpenSAML2 Java library for construction of the XML SAML
 * documents.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class AuthnRequestFactory {

	private AuthnRequestFactory() {
		// empty
	}

	static {
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

	/**
	 * Creates a SAML2 authentication request.
	 * 
	 * @param applicationName
	 * @param applicationKeyPair
	 * @return
	 */
	public static String createAuthnRequest(String applicationName,
			KeyPair applicationKeyPair) {
		if (null == applicationKeyPair) {
			throw new IllegalArgumentException(
					"application key pair should not be null");
		}
		if (null == applicationName) {
			throw new IllegalArgumentException(
					"application name should not be null");
		}

		AuthnRequest request = buildXMLObject(AuthnRequest.class,
				AuthnRequest.DEFAULT_ELEMENT_NAME);

		request.setForceAuthn(true);
		SecureRandomIdentifierGenerator idGenerator;
		try {
			idGenerator = new SecureRandomIdentifierGenerator();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("secure random init error: "
					+ e.getMessage(), e);
		}
		request.setID(idGenerator.generateIdentifier());
		request.setVersion(SAMLVersion.VERSION_20);
		request.setIssueInstant(new DateTime());
		Issuer issuer = buildXMLObject(Issuer.class,
				Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(applicationName);
		request.setIssuer(issuer);

		XMLObjectBuilderFactory builderFactory = Configuration
				.getBuilderFactory();
		SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory
				.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
		Signature signature = signatureBuilder.buildObject();
		signature
				.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		String algorithm = applicationKeyPair.getPrivate().getAlgorithm();
		if ("RSA".equals(algorithm)) {
			signature
					.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);
		} else if ("DSA".equals(algorithm)) {
			signature
					.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_DSA);
		}
		signature.getContentReferences().add(
				new SAMLObjectContentReference(request));
		request.setSignature(signature);
		BasicCredential signingCredential = SecurityHelper
				.getSimpleCredential(applicationKeyPair.getPublic(),
						applicationKeyPair.getPrivate());
		signature.setSigningCredential(signingCredential);

		// marshalling
		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(request);
		Element requestElement;
		try {
			requestElement = marshaller.marshall(request);
		} catch (MarshallingException e) {
			throw new RuntimeException("opensaml2 marshalling error: "
					+ e.getMessage(), e);
		}

		// sign after marshalling of course
		Signer.signObject(signature);

		String result;
		try {
			result = DomUtils.domToString(requestElement);
		} catch (TransformerException e) {
			throw new RuntimeException(
					"DOM to string error: " + e.getMessage(), e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <Type extends SAMLObject> Type buildXMLObject(
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
