/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.webapp;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.demo.keystore.DemoKeyStore;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AttributeBean {

    private static final Log LOG = LogFactory.getLog(AttributeBean.class);

    private String           attributeName;

    private String           attributeWebServiceLocation;

    private String           attributeValue;

    private String           subjectLogin;

    private X509Certificate  certificate;

    private PrivateKey       privateKey;


    public String getSubjectLogin() {

        return subjectLogin;
    }

    public void setSubjectLogin(String subjectLogin) {

        this.subjectLogin = subjectLogin;
    }

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    public String getAttributeWebServiceLocation() {

        return attributeWebServiceLocation;
    }

    public void setAttributeWebServiceLocation(String attributeWebServiceLocation) {

        this.attributeWebServiceLocation = attributeWebServiceLocation;
    }

    public X509Certificate getCertificate() {

        return certificate;
    }

    public PrivateKey getPrivateKey() {

        return privateKey;
    }

    private void loadCertificate() {

        PrivateKeyEntry privateKeyEntry = DemoKeyStore.getPrivateKeyEntry();
        certificate = (X509Certificate) privateKeyEntry.getCertificate();
        privateKey = privateKeyEntry.getPrivateKey();
    }

    public String getAttributeValue() {

        if (null == attributeValue) {
            loadCertificate();
            AttributeClient attributeClient = new AttributeClientImpl(attributeWebServiceLocation, certificate, privateKey);
            try {
                attributeValue = attributeClient.getAttributeValue(subjectLogin, attributeName, String.class);
            } catch (AttributeNotFoundException e) {
                LOG.error("attribute not found: " + e.getMessage());
                return "[attribute not found]";
            } catch (RequestDeniedException e) {
                LOG.error("request denied");
                return "[request denied]";
            } catch (WSClientTransportException e) {
                LOG.error("connection error. check your SSL setup");
                return "[connection error. check your SSL setup]";
            } catch (AttributeUnavailableException e) {
                LOG.error("attribute unavailable: " + attributeName);
                return "[attribute unavailable: " + attributeName + "]";
            }
        }
        return attributeValue;
    }
}
