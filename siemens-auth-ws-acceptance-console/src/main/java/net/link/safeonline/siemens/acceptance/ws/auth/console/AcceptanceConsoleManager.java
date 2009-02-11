/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Observable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import net.link.safeonline.saml.common.Saml2SubjectConfirmationMethod;
import net.link.safeonline.saml.common.Saml2Util;
import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.auth.AuthenticationClient;
import net.link.safeonline.sdk.ws.auth.AuthenticationClientImpl;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClient;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.siemens.acceptance.ws.auth.client.MetroWSAuthenticationClient;
import net.link.safeonline.siemens.acceptance.ws.auth.client.MetroWSAuthenticationClientImpl;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client.SiemensAuthWsAcceptanceClient;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client.SiemensAuthWsAcceptanceClientImpl;
import oasis.names.tc.saml._2_0.assertion.AssertionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;


/**
 * 
 * Application console data class, observable for other registered observers
 * 
 * @author wvdhaute
 */
public class AcceptanceConsoleManager extends Observable {

    static final Log                        LOG             = LogFactory.getLog(AcceptanceConsoleManager.class);

    private static AcceptanceConsoleManager manager         = null;

    private String                          location        = "https://localhost:8443/safe-online-auth-ws";

    private String                          application     = "olas-user";

    private boolean                         generateKeyPair = true;
    private KeyPair                         keyPair;
    private X509Certificate                 certificate;

    private KeyPair                         svKeyPair;
    private X509Certificate                 svCertificate;

    private boolean                         usePcscApplet   = false;

    private AuthenticationClient            authenticationClient;

    private String                          userId;
    private String                          deviceName;


    public static AcceptanceConsoleManager getInstance() {

        if (null == manager) {
            manager = new AcceptanceConsoleManager();
        }
        return manager;
    }

    private AcceptanceConsoleManager() {

        // empty
    }

    public AuthenticationClient getAuthenticationClient()
            throws RequestDeniedException, WSClientTransportException {

        if (null == authenticationClient) {
            GetAuthenticationClient getAuthenticationClient = new GetAuthenticationClientImpl(location);
            authenticationClient = new AuthenticationClientImpl(getAuthenticationClient.getInstance());
        }
        return authenticationClient;
    }

    public void resetAuthenticationClient() {

        authenticationClient = null;
    }

    public SiemensAuthWsAcceptanceClient getAcceptanceWsClient() {

        if (null == authenticationClient || null == authenticationClient.getAssertion())
            return null;

        if (null != keyPair)
            // holder-of-key
            return new SiemensAuthWsAcceptanceClientImpl("http://localhost:8080", authenticationClient.getAssertion(), certificate,
                    keyPair.getPrivate());

        // sender-vouches
        try {
            svKeyPair = KeyStoreUtils.generateKeyPair();
            svCertificate = KeyStoreUtils.generateSelfSignedCertificate(svKeyPair, "cn=Acceptance Test Console");
        } catch (Exception e) {
            LOG.error("Exception: " + e.getCause().getClass().getName() + " message=" + e.getMessage(), e);
            return null;
        }

        return new SiemensAuthWsAcceptanceClientImpl("http://localhost:8080", authenticationClient.getAssertion(), svCertificate,
                svKeyPair.getPrivate());
    }

    @Deprecated
    public MetroWSAuthenticationClient getAcceptanceMetroWsClient() {

        try {
            AssertionType assertion = null;
            Assertion samlAssertion = Saml2Util.getAssertion("test-in-response-to", "test-audience", "test-subject", "test-issuer",
                    "test-saml-name", 10000, null, new DateTime(), Saml2SubjectConfirmationMethod.SENDER_VOUCHES, getPublicKey());
            MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(samlAssertion);
            Element assertionElement = null;
            try {
                assertionElement = marshaller.marshall(samlAssertion);
            } catch (MarshallingException e) {
                LOG.debug("marshal exception: " + e.getMessage());
                throw new RuntimeException("opensaml2 marshalling error: " + e.getMessage(), e);
            }

            JAXBContext context;
            try {
                context = JAXBContext.newInstance(AssertionType.class);
                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();
                JAXBElement<?> assertionObject = (JAXBElement<?>) unmarshaller.unmarshal(assertionElement);
                assertion = (AssertionType) assertionObject.getValue();
            } catch (JAXBException e) {
                LOG.debug("jaxb exception: " + e.getMessage());
                return null;
            }

            MetroWSAuthenticationClient client = new MetroWSAuthenticationClientImpl("http://localhost:8080", assertion, certificate,
                    keyPair.getPrivate());
            return client;
        } catch (Exception e) {
            LOG.debug("exception: " + e.getMessage());
            return null;
        }
    }

    public void setLocation(String location) {

        this.location = location;
        setChanged();
        notifyObservers();
    }

    public String getLocation() {

        return location;
    }

    public void setApplication(String application) {

        this.application = application;
        setChanged();
        notifyObservers();
    }

    public String getApplication() {

        return application;
    }

    public boolean getGenerateKeyPair() {

        return generateKeyPair;
    }

    public void setGenerateKeyPair(boolean generateKeyPair) {

        this.generateKeyPair = generateKeyPair;
    }

    public PublicKey getPublicKey()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalStateException,
            SignatureException, CertificateException, IOException {

        if (generateKeyPair) {
            keyPair = KeyStoreUtils.generateKeyPair();
            certificate = KeyStoreUtils.generateSelfSignedCertificate(keyPair, "cn=Test");
            return keyPair.getPublic();
        }

        keyPair = null;
        certificate = null;
        return null;
    }

    public boolean getUsePcscApplet() {

        return usePcscApplet;
    }

    public void setUsePcscApplet(boolean usePcscApplet) {

        this.usePcscApplet = usePcscApplet;
    }

    public void setDeviceName(String deviceName) {

        this.deviceName = deviceName;
    }

    public String getDeviceName() {

        return deviceName;
    }

    public void setUserId(String userId) {

        this.userId = userId;
        setChanged();
        notifyObservers();
    }

    public String getUserId() {

        return userId;
    }
}
