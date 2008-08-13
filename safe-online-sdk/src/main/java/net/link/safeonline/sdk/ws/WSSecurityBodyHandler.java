/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.security.cert.X509Certificate;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;


/**
 * JAX-WS SOAP Handler to verify the digestion of the SOAP Body element by the WS-Security signature. We have to
 * postpone this verification until after we know the calling application identity since we need to be able to determine
 * if we need to perform the check on an application basis.
 *
 * @author fcorneli
 *
 */
public class WSSecurityBodyHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log               LOG = LogFactory.getLog(WSSecurityBodyHandler.class);

    private WSSecurityConfigurationService wsSecurityConfigurationService;

    private String                         wsSecurityConfigurationServiceJndiName;


    @PostConstruct
    public void postConstructCallback() {

        loadDependencies();
        this.wsSecurityConfigurationService = EjbUtils.getEJB(this.wsSecurityConfigurationServiceJndiName,
                WSSecurityConfigurationService.class);
    }

    private void loadDependencies() {

        try {
            Context ctx = new javax.naming.InitialContext();
            Context env = (Context) ctx.lookup("java:comp/env");
            this.wsSecurityConfigurationServiceJndiName = (String) env.lookup("wsSecurityConfigurationServiceJndiName");
        } catch (NamingException e) {
            LOG.debug("naming exception: " + e.getMessage());
            throw new RuntimeException("WS Security Configuration JNDI path not specified");
        }
    }

    public Set<QName> getHeaders() {

        return null;
    }

    public void close(MessageContext messageContext) {

    }

    public boolean handleFault(SOAPMessageContext soapMessageContext) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {

        Boolean outboundProperty = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (true == outboundProperty)
            return true;

        X509Certificate certificate = WSSecurityServerHandler.getCertificate(soapMessageContext);
        if (null == certificate)
            throw new RuntimeException("no certificate found on JAX-WS context");

        boolean skipMessageIntegrityCheck = this.wsSecurityConfigurationService.skipMessageIntegrityCheck(certificate);

        if (true == skipMessageIntegrityCheck) {
            LOG.debug("skipping message integrity check");
            return true;
        }

        LOG.debug("performing message integrity check");
        /*
         * Check whether the SOAP Body has been signed.
         */
        SOAPMessage soapMessage = soapMessageContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPBody soapBody;
        try {
            soapBody = soapPart.getEnvelope().getBody();
        } catch (SOAPException e) {
            throw WSSecurityUtil.createSOAPFaultException("error retrieving SOAP Body", "FailedCheck");
        }
        String bodyId = soapBody.getAttributeNS(WSConstants.WSU_NS, "Id");
        if (null == bodyId || 0 == bodyId.length())
            throw WSSecurityUtil.createSOAPFaultException("SOAP Body should have a wsu:Id attribute", "FailedCheck");
        boolean isBodySigned = WSSecurityServerHandler.isSignedElement(bodyId, soapMessageContext);
        if (false == isBodySigned)
            throw WSSecurityUtil.createSOAPFaultException("SOAP Body was not signed", "FailedCheck");

        return true;
    }
}
