/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * W3C DOM utility class.
 * 
 * @author fcorneli
 * 
 */
public class DomUtils {

	private DomUtils() {
		// empty
	}

	/**
	 * Parses the given string to a DOM object.
	 * 
	 * @param documentString
	 * @throws Exception
	 */
	public static Document parseDocument(String documentString)
			throws Exception {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
		StringReader stringReader = new StringReader(documentString);
		InputSource inputSource = new InputSource(stringReader);
		return domBuilder.parse(inputSource);
	}

	/**
	 * Saves a DOM document to the given output file.
	 * 
	 * @param document
	 * @param outputFile
	 * @throws TransformerException
	 */
	public static void saveDocument(Document document, File outputFile)
			throws TransformerException {
		Source source = new DOMSource(document);
		Result streamResult = new StreamResult(outputFile);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(source, streamResult);
	}

	/**
	 * Loads a DOM document from the given input stream.
	 * 
	 * @param documentInputStream
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document loadDocument(InputStream documentInputStream)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder;
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(documentInputStream);
		return document;
	}

	/**
	 * Transforms a DOM node (e.g. DOM element or DOM document) to a String.
	 * 
	 * @param domNode
	 * @throws TransformerException
	 */
	public static String domToString(Node domNode) throws TransformerException {
		Source source = new DOMSource(domNode);
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, result);
		return stringWriter.toString();
	}
}
