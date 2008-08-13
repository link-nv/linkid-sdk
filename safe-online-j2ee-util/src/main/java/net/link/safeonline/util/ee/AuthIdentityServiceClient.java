/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.ee;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mx.util.MBeanServerLocator;


/**
 * Client for SafeOnline Authentication Identity Service JMX bean. This service manages the key pair of the SafeOnline
 * service itself. See also: safe-online-service.
 *
 * @author fcorneli
 *
 */
public class AuthIdentityServiceClient {

    private static final Log   LOG                   = LogFactory.getLog(AuthIdentityServiceClient.class);

    private final MBeanServer  server;

    private final ObjectName   authIdentityServiceName;

    public static final String AUTH_IDENTITY_SERVICE = "safeonline:service=AuthIdentity";


    /**
     * Main constructor.
     */
    public AuthIdentityServiceClient() {

        this.server = MBeanServerLocator.locateJBoss();
        LOG.debug("MBean Server class: " + this.server.getClass().getName());
        try {
            this.authIdentityServiceName = new ObjectName(AUTH_IDENTITY_SERVICE);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("object name error: " + e.getMessage());
        }
    }

    /**
     * Gives back the private key of the SafeOnline service entity.
     *
     */
    public PrivateKey getPrivateKey() {

        Object[] params = {};
        String[] signature = {};
        PrivateKey privateKey;
        try {
            privateKey = (PrivateKey) this.server.invoke(this.authIdentityServiceName, "getPrivateKey", params,
                    signature);
        } catch (Exception e) {
            throw new RuntimeException("invoke error: " + e.getMessage(), e);
        }
        return privateKey;
    }

    /**
     * Gives back the public key of the SafeOnline service entity.
     *
     */
    public PublicKey getPublicKey() {

        Object[] params = {};
        String[] signature = {};
        PublicKey publicKey;
        try {
            publicKey = (PublicKey) this.server.invoke(this.authIdentityServiceName, "getPublicKey", params, signature);
        } catch (Exception e) {
            throw new RuntimeException("invoke error: " + e.getMessage(), e);
        }
        return publicKey;
    }

    /**
     * Gives back the X509 certificate of the SafeOnline service entity.
     *
     */
    public X509Certificate getCertificate() {

        Object[] params = {};
        String[] signature = {};
        X509Certificate certificate;
        try {
            certificate = (X509Certificate) this.server.invoke(this.authIdentityServiceName, "getCertificate", params,
                    signature);
        } catch (Exception e) {
            throw new RuntimeException("invoke error: " + e.getMessage(), e);
        }
        return certificate;
    }
}
