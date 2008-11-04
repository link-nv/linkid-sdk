/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.sdk.ws.WSSecurityServerHandler;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.auth.callback.UsernamePasswordHandler;


/**
 * JAX-WS Login Handler. This JAX-WS SOAP handler will perform the JAAS login for applications, devices or olas nodes.
 * 
 * The JAAS login is performed using certificate and the id ( application name, device name, olas node name ).
 * 
 * @author fcorneli
 * 
 */
public class LoginHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log    LOG                   = LogFactory.getLog(LoginHandler.class);

    private static final String LOGINCONTEXT_PROPERTY = "net.link.safeonline.logincontext";


    public Set<QName> getHeaders() {

        return null;
    }

    @SuppressWarnings("unused")
    public void close(MessageContext context) {

        // empty
    }

    @SuppressWarnings("unused")
    public boolean handleFault(SOAPMessageContext context) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext context) {

        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (false == outboundProperty.booleanValue()) {
            login(context);
            return true;
        }
        logout(context);
        return true;
    }

    private void login(SOAPMessageContext context) {

        LOG.debug("login");

        X509Certificate certificate = WSSecurityServerHandler.getCertificate(context);
        if (null == certificate)
            throw new RuntimeException("no certificate found on JAX-WS context");

        String id = CertificateMapperHandler.getId(context);
        try {
            char[] password = toPassword(certificate);
            UsernamePasswordHandler callbackHandler = new UsernamePasswordHandler(id, password);
            LoginContext loginContext = new LoginContext("client-login", callbackHandler);
            LOG.debug("performing login for " + id);
            loginContext.login();
            context.put(LOGINCONTEXT_PROPERTY, loginContext);
        } catch (LoginException e) {
            throw new RuntimeException("JAAS login error: " + e.getMessage());
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("cert encoding error: " + e.getMessage());
        }
    }

    private void logout(SOAPMessageContext context) {

        LOG.debug("logout");

        X509Certificate certificate = WSSecurityServerHandler.getCertificate(context);
        if (null == certificate)
            throw new RuntimeException("no certificate found on JAX-WS context");

        LoginContext loginContext = (LoginContext) context.get(LOGINCONTEXT_PROPERTY);
        if (null == loginContext)
            throw new RuntimeException("no JAAS login context present on the JAX-WS context");
        try {
            loginContext.logout();
        } catch (LoginException e) {
            throw new RuntimeException("JAAS logout error");
        }
    }

    private char[] toPassword(X509Certificate certificate)
            throws CertificateEncodingException {

        byte[] encodedCertificate = certificate.getEncoded();
        char[] password = Hex.encodeHex(encodedCertificate);
        return password;
    }
}
