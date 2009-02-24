/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
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

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sdk.ws.WSSecurityServerHandler;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.util.CertificateMapperHandler;
import net.link.safeonline.ws.util.CertificateValidatorHandler;
import net.link.safeonline.ws.util.CertificateValidatorHandler.CertificateDomain;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.SubjectType;
import oasis.names.tc.saml._2_0.protocol.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * SOAP Handler for TargetIdentity SOAP Header handling. This SOAP handler will check for the presence of the TargetIdentity SOAP Header. If
 * present it will push the found subject name onto the messaging context.
 * 
 * <p>
 * Specifications: Liberty ID-WSF SOAP Binding Specification 2.0
 * </p>
 * 
 * @author fcorneli
 */
public class TargetIdentityHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log               LOG                         = LogFactory.getLog(TargetIdentityHandler.class);

    public static final String             TARGET_IDENTITY_CONTEXT_VAR = TargetIdentityHandler.class.getName() + ".TargetIdentity";

    public static final String             WSU_NS                      = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    private static final QName             TARGET_IDENTITY_NAME        = new QName(DataServiceConstants.LIBERTY_SOAP_BINDING_NAMESPACE,
                                                                               "TargetIdentity");

    private WSSecurityConfigurationService wsSecurityConfigurationService;

    private String                         wsSecurityConfigurationServiceJndiName;

    private CertificateDomain              certificateDomain;


    @PostConstruct
    public void postConstructCallback() {

        loadDependencies();
        wsSecurityConfigurationService = EjbUtils.getEJB(wsSecurityConfigurationServiceJndiName, WSSecurityConfigurationService.class);
    }

    private void loadDependencies() {

        try {
            Context ctx = new javax.naming.InitialContext();
            Context env = (Context) ctx.lookup("java:comp/env");
            wsSecurityConfigurationServiceJndiName = (String) env.lookup("wsSecurityConfigurationServiceJndiName");
        } catch (NamingException e) {
            LOG.debug("naming exception: " + e.getMessage());
            throw new RuntimeException("WS Security Configuration JNDI path not specified");
        }
    }

    public Set<QName> getHeaders() {

        Set<QName> headers = new HashSet<QName>();
        /*
         * Communicate to the JAX-WS web service stack that this handler can handle the TargetIdentity SOAP header element.
         */
        headers.add(TARGET_IDENTITY_NAME);
        return headers;
    }

    public void close(@SuppressWarnings("unused") MessageContext context) {

        LOG.debug("close");
    }

    public boolean handleFault(@SuppressWarnings("unused") SOAPMessageContext soapContext) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext soapContext) {

        Boolean outboundProperty = (Boolean) soapContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (true == outboundProperty.booleanValue())
            /*
             * We only need to verify the TargetIdentity SOAP header on inbound messages.
             */
            return true;

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

    private void processHeaders(SOAPHeader soapHeader, SOAPMessageContext soapContext)
            throws JAXBException {

        LOG.debug("processing headers");
        Iterator<?> iterator = soapHeader.examineAllHeaderElements();
        while (iterator.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement) iterator.next();
            QName elementName = headerElement.getElementQName();
            if (true == TARGET_IDENTITY_NAME.equals(elementName)) {
                processTargetIdentityHeader(headerElement, soapContext);
            }
        }
    }

    private void processTargetIdentityHeader(SOAPHeaderElement targetIdentityHeaderElement, SOAPMessageContext soapContext)
            throws JAXBException {

        LOG.debug("processing TargetIdentity header");

        X509Certificate certificate = WSSecurityServerHandler.getCertificate(soapContext);
        if (null == certificate)
            throw new RuntimeException("no certificate found on JAX-WS context");

        boolean skipMessageIntegrityCheck = wsSecurityConfigurationService.skipMessageIntegrityCheck(certificate);

        if (!skipMessageIntegrityCheck) {
            /*
             * First check whether the TargetIdentity SOAP header has been digested correctly by the WS-Security XML signature.
             */
            String id = targetIdentityHeaderElement.getAttributeNS(WSU_NS, "Id");
            if (null == id)
                throw new RuntimeException("wsu:Id attribute not found");
            boolean signed = WSSecurityServerHandler.isSignedElement(id, soapContext);
            if (false == signed)
                throw new RuntimeException("TargetIdentity SOAP header not signed by WS-Security");
        }

        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshaller.unmarshal(targetIdentityHeaderElement.getFirstChild());

        Object element = jaxbElement.getValue();
        if (false == element instanceof SubjectType)
            throw new RuntimeException("samlp:Subject expected");

        certificateDomain = CertificateValidatorHandler.getCertificateDomain(soapContext);
        LOG.debug("certificate domain: " + certificateDomain.toString());

        SubjectType subject = (SubjectType) element;
        String login = findSubjectLogin(subject);

        String userId = null;
        try {
            userId = findUserId(CertificateMapperHandler.getId(soapContext), login);
        } catch (ApplicationNotFoundException e) {
            throw new RuntimeException("application on JAX-WS context not found");
        }
        if (null == userId)
            throw new RuntimeException("user ID on JAX-WS context not found");

        LOG.debug("TargetIdentity: " + userId);
        soapContext.put(TARGET_IDENTITY_CONTEXT_VAR, userId);
        LOG.debug("scope: " + soapContext.getScope(TARGET_IDENTITY_CONTEXT_VAR));
        /*
         * We need to set the scope to APPLICATION, else the port implementation will not be able to retrieve the value via its web service
         * context.
         */
        soapContext.setScope(TARGET_IDENTITY_CONTEXT_VAR, Scope.APPLICATION);
    }

    private String findSubjectLogin(SubjectType subject) {

        List<JAXBElement<?>> subjectContent = subject.getContent();
        for (JAXBElement<?> subjectItem : subjectContent) {
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

    private String findUserId(String id, String userId)
            throws ApplicationNotFoundException {

        if (certificateDomain.equals(CertificateDomain.APPLICATION)) {
            long applicationId = Long.parseLong(id);
            ApplicationIdentifierMappingService applicationIdentifierMappingService = EjbUtils.getEJB(
                    ApplicationIdentifierMappingService.JNDI_BINDING, ApplicationIdentifierMappingService.class);
            return applicationIdentifierMappingService.findUserId(applicationId, userId);
        } else if (certificateDomain.equals(CertificateDomain.NODE)) {
            NodeMappingService nodeMappingService = EjbUtils.getEJB(NodeMappingService.JNDI_BINDING, NodeMappingService.class);
            try {
                return nodeMappingService.getNodeMapping(userId).getSubject().getUserId();
            } catch (NodeMappingNotFoundException e) {
                LOG.error("Node mapping not found", e);
                return null;
            }
        }

        return null;
    }

    /**
     * Gives back the target identity. This target identity has been extracted before by this handler from the TargetIdentity SOAP header.
     * 
     * @param context
     * @throws TargetIdentityException
     *             in case of a missing TargetIdentity SOAP header.
     */
    public static String getTargetIdentity(WebServiceContext context)
            throws TargetIdentityException {

        MessageContext messageContext = context.getMessageContext();
        String targetIdentity = (String) messageContext.get(TargetIdentityHandler.TARGET_IDENTITY_CONTEXT_VAR);
        if (null == targetIdentity)
            throw new TargetIdentityException();
        return targetIdentity;
    }
}
