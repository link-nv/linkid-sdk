/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.data.ws.DataServiceConstants;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;

import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.assertion.SubjectType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SOAP Handler for TargetIdentity SOAP Header handling.
 * 
 * Specifications: Liberty ID-WSF SOAP Binding Specification 2.0
 * 
 * @author fcorneli
 * 
 */
public class TargetIdentityClientHandler implements
		SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory
			.getLog(TargetIdentityClientHandler.class);

	public static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	public static final String WSU_PREFIX = "wsu";

	public static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

	private String targetIdentity;

	/**
	 * Sets the target identity, i.e. the user Id.
	 * 
	 * @param targetIdentity
	 */
	public void setTargetIdentity(String targetIdentity) {
		this.targetIdentity = targetIdentity;
	}

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
		if (false == outboundProperty.booleanValue()) {
			/*
			 * We only need to add the TargetIdentity SOAP header to the
			 * outbound messages.
			 */
			return true;
		}

		SOAPMessage soapMessage = soapContext.getMessage();
		SOAPHeader soapHeader;
		try {
			soapHeader = soapMessage.getSOAPHeader();
			if (soapHeader == null) {
				/*
				 * This can happen in the case that we're the first one to add a
				 * SOAP header element.
				 */
				soapHeader = soapMessage.getSOAPPart().getEnvelope()
						.addHeader();
			}
			addTargetIdentityHeader(soapHeader, soapContext);
		} catch (SOAPException e) {
			throw new RuntimeException("SOAP error: " + e.getMessage(), e);
		} catch (JAXBException e) {
			throw new RuntimeException("JAXB error: " + e.getMessage(), e);
		}
		return true;
	}

	private void addTargetIdentityHeader(SOAPHeader soapHeader,
			SOAPMessageContext soapContext) throws SOAPException, JAXBException {
		if (null == this.targetIdentity) {
			throw new IllegalStateException("TargetIdentity is null");
		}
		LOG.debug("adding TargetIdentity: " + this.targetIdentity);

		/*
		 * Add SOAP Header.
		 */
		QName targetIdentityName = new QName(
				DataServiceConstants.LIBERTY_SOAP_BINDING_NAMESPACE,
				"TargetIdentity");
		SOAPHeaderElement targetIdentityHeaderElement = soapHeader
				.addHeaderElement(targetIdentityName);
		targetIdentityHeaderElement.setMustUnderstand(true);

		/*
		 * Make sure that the WS-Security JAX-WS handler will include the
		 * TargetIdentity SOAP header element in the signature digest.
		 */
		String id = "id-" + UUID.randomUUID().toString();
		targetIdentityHeaderElement.setAttributeNS(XMLNS_NS, "xmlns:"
				+ WSU_PREFIX, WSU_NS);
		targetIdentityHeaderElement.setAttributeNS(WSU_NS, WSU_PREFIX + ":Id",
				id);
		WSSecurityClientHandler.addToBeSignedId(id, soapContext);

		/*
		 * Create header content in JAXB.
		 */
		ObjectFactory objectFactory = new ObjectFactory();
		SubjectType subject = objectFactory.createSubjectType();
		NameIDType subjectName = new NameIDType();
		subjectName.setValue(this.targetIdentity);
		subject.getContent().add(objectFactory.createNameID(subjectName));

		/*
		 * Add header element content to header element.
		 */
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(objectFactory.createSubject(subject),
				targetIdentityHeaderElement);
	}
}
