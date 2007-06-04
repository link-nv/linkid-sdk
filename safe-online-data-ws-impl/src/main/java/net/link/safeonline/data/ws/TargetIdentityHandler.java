/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.ws.util.WSSecurityServerHandler;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.SubjectType;
import oasis.names.tc.saml._2_0.protocol.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SOAP Handler for TargetIdentity SOAP Header handling. This SOAP handler will
 * check for the presence of the TargetIdentity SOAP Header. If present it will
 * push the found subject name onto the messaging context.
 * 
 * <p>
 * Specifications: Liberty ID-WSF SOAP Binding Specification 2.0
 * </p>
 * 
 * @author fcorneli
 */
public class TargetIdentityHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory
			.getLog(TargetIdentityHandler.class);

	public static final String TARGET_IDENTITY_CONTEXT_VAR = TargetIdentityHandler.class
			.getName()
			+ ".TargetIdentity";

	public static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	private static final QName TARGET_IDENTITY_NAME = new QName(
			DataServiceConstants.LIBERTY_SOAP_BINDING_NAMESPACE,
			"TargetIdentity");

	public Set<QName> getHeaders() {
		Set<QName> headers = new HashSet<QName>();
		/*
		 * Communicate to the JAX-WS web service stack that this handler can
		 * handle the TargetIdentity SOAP header element.
		 */
		headers.add(TARGET_IDENTITY_NAME);
		return headers;
	}

	public void close(MessageContext context) {
		LOG.debug("close");
	}

	public boolean handleFault(SOAPMessageContext soapContext) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext soapContext) {
		Boolean outboundProperty = (Boolean) soapContext
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (true == outboundProperty.booleanValue()) {
			/*
			 * We only need to verify the TargetIdentity SOAP header on inbound
			 * messages.
			 */
			return true;
		}

		SOAPMessage soapMessage = soapContext.getMessage();
		try {
			SOAPHeader soapHeader = soapMessage.getSOAPHeader();
			processHeaders(soapHeader, soapContext);
		} catch (SOAPException e) {
			throw new RuntimeException("SOAP error: " + e.getMessage(), e);
		} catch (JAXBException e) {
			throw new RuntimeException("JAXB error: " + e.getMessage(), e);
		}

		LOG.debug("done.");
		return true;
	}

	private void processHeaders(SOAPHeader soapHeader,
			SOAPMessageContext soapContext) throws JAXBException {
		LOG.debug("processing headers");
		Iterator iterator = soapHeader.examineAllHeaderElements();
		while (iterator.hasNext()) {
			SOAPHeaderElement headerElement = (SOAPHeaderElement) iterator
					.next();
			QName elementName = headerElement.getElementQName();
			if (true == TARGET_IDENTITY_NAME.equals(elementName)) {
				processTargetIdentityHeader(headerElement, soapContext);
			}
		}
	}

	private void processTargetIdentityHeader(
			SOAPHeaderElement targetIdentityHeaderElement,
			SOAPMessageContext soapContext) throws JAXBException {
		LOG.debug("processing TargetIdentity header");

		/*
		 * First check whether the TargetIdentity SOAP header has been digested
		 * correcly by the WS-Security XML signature.
		 */
		String id = targetIdentityHeaderElement.getAttributeNS(WSU_NS, "Id");
		if (null == id) {
			throw new RuntimeException("wsu:Id attribute not found");
		}
		boolean signed = WSSecurityServerHandler.isSignedElement(id,
				soapContext);
		if (false == signed) {
			throw new RuntimeException(
					"TargetIdentity SOAP header not signed by WS-Security");
		}

		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement jaxbElement = (JAXBElement) unmarshaller
				.unmarshal(targetIdentityHeaderElement.getFirstChild());

		Object element = jaxbElement.getValue();
		if (false == element instanceof SubjectType) {
			throw new RuntimeException("samlp:Subject expected");
		}

		SubjectType subject = (SubjectType) element;
		String login = findSubjectLogin(subject);

		LOG.debug("TargetIdentity: " + login);
		soapContext.put(TARGET_IDENTITY_CONTEXT_VAR, login);
		LOG
				.debug("scope: "
						+ soapContext.getScope(TARGET_IDENTITY_CONTEXT_VAR));
		/*
		 * We need to set the scope to APPLICATION, else the port implementation
		 * will not be able to retrieve the value via its web service context.
		 */
		soapContext.setScope(TARGET_IDENTITY_CONTEXT_VAR, Scope.APPLICATION);
	}

	private String findSubjectLogin(SubjectType subject) {
		List<JAXBElement<?>> subjectContent = subject.getContent();
		for (JAXBElement subjectItem : subjectContent) {
			Object value = subjectItem.getValue();
			if (false == value instanceof NameIDType) {
				continue;
			}
			NameIDType nameId = (NameIDType) value;
			String subjectLogin = nameId.getValue();
			return subjectLogin;
		}
		return null;
	}

	/**
	 * Gives back the target identity. This target identity has been extracted
	 * before by this handler from the TargetIdentity SOAP header.
	 * 
	 * @param context
	 * @return
	 * @throws TargetIdentityException
	 *             in case of a missing TargetIdentity SOAP header.
	 */
	public static String getTargetIdentity(WebServiceContext context)
			throws TargetIdentityException {
		MessageContext messageContext = context.getMessageContext();
		String targetIdentity = (String) messageContext
				.get(TargetIdentityHandler.TARGET_IDENTITY_CONTEXT_VAR);
		if (null == targetIdentity) {
			throw new TargetIdentityException();
		}
		return targetIdentity;
	}
}
