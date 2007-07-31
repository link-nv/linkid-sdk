/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline.saml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;

import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.joda.time.DateTime;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.artifact.SAMLArtifactFactory;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.w3c.dom.Element;

public class SamlTest {

	private static final Log LOG = LogFactory.getLog(SamlTest.class);

	@SuppressWarnings("unchecked")
	@Test
	public void samlAssertion() throws Exception {
		System
				.setProperty(
						"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
						"org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		DefaultBootstrap.bootstrap();

		LOG.debug("SAML request test");
		SAMLArtifactFactory artifactFactory = Configuration
				.getArtifactFactory();

		assertNotNull(artifactFactory);

		XMLObjectBuilderFactory builderFactory = Configuration
				.getBuilderFactory();
		SAMLObjectBuilder<Assertion> assertionBuilder = (SAMLObjectBuilder<Assertion>) builderFactory
				.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);

		Assertion assertion = assertionBuilder.buildObject();
		assertion.setVersion(SAMLVersion.VERSION_20);

		SecureRandomIdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();

		assertion.setID(idGenerator.generateIdentifier());
		assertion.setIssueInstant(new DateTime());

		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(assertion);
		Element assertionElement = marshaller.marshall(assertion);

		LOG.debug("result assertion: "
				+ DomTestUtils.domToString(assertionElement));
		assertNotNull(XPathAPI.selectNodeIterator(assertionElement,
				"saml:Assertion"));
	}

	@Test
	public void testAuthnRequest() throws Exception {
		System
				.setProperty(
						"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
						"org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		DefaultBootstrap.bootstrap();

		KeyPair testKeyPair = PkiTestUtils.generateKeyPair();
		PrivateKey privateKey = testKeyPair.getPrivate();
		PublicKey publicKey = testKeyPair.getPublic();

		AuthnRequest request = buildXMLObject(AuthnRequest.class,
				AuthnRequest.DEFAULT_ELEMENT_NAME);

		request.setForceAuthn(true);
		SecureRandomIdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();
		request.setID(idGenerator.generateIdentifier());
		request.setVersion(SAMLVersion.VERSION_20);
		request.setIssueInstant(new DateTime());
		Issuer issuer = buildXMLObject(Issuer.class,
				Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue("test-application-id");
		request.setIssuer(issuer);

		XMLObjectBuilderFactory builderFactory = Configuration
				.getBuilderFactory();
		SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory
				.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
		Signature signature = signatureBuilder.buildObject();
		signature
				.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		signature
				.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);
		signature.getContentReferences().add(
				new SAMLObjectContentReference(request));
		request.setSignature(signature);
		BasicCredential signingCredential = SecurityHelper.getSimpleCredential(
				publicKey, privateKey);
		signature.setSigningCredential(signingCredential);

		// verify marshalling
		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(request);
		Element requestElement = marshaller.marshall(request);

		// sign after marshalling of course
		Signer.signObject(signature);

		LOG
				.debug("result request: "
						+ DomTestUtils.domToString(requestElement));
	}

	@SuppressWarnings("unchecked")
	private static <Type extends SAMLObject> Type buildXMLObject(
			Class<Type> clazz, QName objectQName) {
		XMLObjectBuilder<Type> builder = Configuration.getBuilderFactory()
				.getBuilder(objectQName);
		if (builder == null) {
			fail("Unable to retrieve builder for object QName " + objectQName);
		}
		Type object = builder.buildObject(objectQName.getNamespaceURI(),
				objectQName.getLocalPart(), objectQName.getPrefix());
		return object;
	}

	@Test
	public void endorsedXercer() throws Exception {
		String builderFactoryClassName = DocumentBuilderFactory.newInstance()
				.getClass().getName();
		LOG.debug("document builder factory class name: "
				+ builderFactoryClassName);
		assertEquals("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl",
				builderFactoryClassName);

		System.setProperty("jaxp.debug", "true");
		System
				.setProperty(
						"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
						"org.apache.xerces.jaxp.validation.XMLSchemaFactory");

		SchemaFactory factory = SchemaFactory
				.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
		String schemaFactoryClassname = factory.getClass().getName();
		LOG.debug("schema factory class: " + schemaFactoryClassname);
		assertEquals("org.apache.xerces.jaxp.validation.XMLSchemaFactory",
				schemaFactoryClassname);
	}
}
