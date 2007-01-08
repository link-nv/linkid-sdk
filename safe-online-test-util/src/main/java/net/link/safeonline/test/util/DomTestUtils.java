/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DomTestUtils {

	private DomTestUtils() {
		// empty
	}

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
}
