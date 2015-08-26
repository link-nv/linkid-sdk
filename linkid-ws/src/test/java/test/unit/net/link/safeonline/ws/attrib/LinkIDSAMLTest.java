/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.attrib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.link.util.logging.Logger;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.assertion.StatementAbstractType;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class LinkIDSAMLTest {

    private static final Logger logger = Logger.get( LinkIDSAMLTest.class );

    @SuppressWarnings("unchecked")
    @Test
    public void attributeValueXsiType()
            throws Exception {

        // Setup Data
        ObjectFactory objectFactory = new ObjectFactory();
        AssertionType assertion = objectFactory.createAssertionType();

        List<StatementAbstractType> statements = assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement();
        AttributeStatementType attributeStatement = objectFactory.createAttributeStatementType();
        statements.add( attributeStatement );

        List<Serializable> attributes = attributeStatement.getAttributeOrEncryptedAttribute();
        AttributeType attribute = objectFactory.createAttributeType();
        attributes.add( attribute );

        attribute.setName( "test-attribute-name" );
        List<Object> attributeValue = attribute.getAttributeValue();

        AttributeType memberAttribute = objectFactory.createAttributeType();
        memberAttribute.setName( "member-attribute-name" );
        memberAttribute.getAttributeValue().add( "value" );
        attributeValue.add( memberAttribute );

        attributeValue.add( "hello world" );
        attributeValue.add( 5 );
        attributeValue.add( true );
        attributeValue.add( new Date() );
        attributeValue.add( 3.14 );

        JAXBContext context = JAXBContext.newInstance( ObjectFactory.class );
        Marshaller marshaller = context.createMarshaller();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // Test
        marshaller.marshal( objectFactory.createAssertion( assertion ), document );

        // Verify
        logger.dbg( "Doc: %s", domToString( document ) );
        Element nsElement = document.createElement( "nsElement" );
        nsElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion" );
        nsElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );

        Node resultNode = XPathAPI.selectSingleNode( document,
                "/saml:Assertion/saml:AttributeStatement/saml:Attribute/saml:AttributeValue[@xsi:type='AttributeType']/saml:AttributeValue", nsElement );
        assertNotNull( resultNode );

        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement<AssertionType> assertionElement = (JAXBElement<AssertionType>) unmarshaller.unmarshal( document );

        assertEquals( AttributeType.class, ((AttributeType) ((AttributeStatementType) assertionElement.getValue()
                                                                                                      .getStatementOrAuthnStatementOrAuthzDecisionStatement()
                                                                                                      .get( 0 )).getAttributeOrEncryptedAttribute()
                                                                                                                .get( 0 )).getAttributeValue()
                                                                                                                          .get( 0 )
                                                                                                                          .getClass() );
    }

    public static String domToString(Node domNode)
            throws TransformerException {

        Source source = new DOMSource( domNode );
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult( stringWriter );
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        transformer.transform( source, result );
        return stringWriter.toString();
    }
}
