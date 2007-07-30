/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline.saml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;

import net.link.safeonline.test.util.DomTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.joda.time.DateTime;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.artifact.SAMLArtifactFactory;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
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
