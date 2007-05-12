/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util;

import java.io.StringWriter;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

/**
 * Log SOAP Handler. Will simply log the inbound and outbound SOAP messages. Can
 * come in handy when debugging web services that run over SSL.
 * 
 * @author fcorneli
 * 
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory.getLog(LoggingHandler.class);

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext context) {
	}

	public boolean handleFault(SOAPMessageContext soapContext) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext soapContext) {
		Boolean outboundProperty = (Boolean) soapContext
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		SOAPMessage soapMessage = soapContext.getMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();
		LOG.debug("SOAP message (outbound: " + outboundProperty + "): "
				+ toString(soapPart));
		return true;
	}

	public static String toString(Node domNode) {
		Source source = new DOMSource(domNode);
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		/*
		 * TransformerFactory nor Transformer are thread safe. Thus we cannot
		 * optimize via some @PostConstruct method to pre-create instances of
		 * these.
		 */
		try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("Transformer config error: "
					+ e.getMessage());
		} catch (TransformerException e) {
			throw new RuntimeException("Transformer error: " + e.getMessage());
		}
		return stringWriter.toString();
	}
}
