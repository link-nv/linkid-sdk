/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.sdk.KeyStoreUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * JMX bean that manages the authentication identity of the SafeOnline instance. This identity is used for authenticating this olas node to
 * other olas nodes.
 * 
 * @author wvdhaute
 * 
 */
public class AuthIdentityService implements AuthIdentityServiceMBean {

    private static final Log LOG = LogFactory.getLog(AuthIdentityService.class);


    public AuthIdentityService() {

        LOG.debug("construction");
    }


    private String          keyStoreResource;

    private String          keyStoreFile;

    private String          keyStorePassword;

    private String          keyStoreType;

    private PrivateKey      privateKey;

    private PublicKey       publicKey;

    private X509Certificate certificate;


    public void loadKeyPair() {

        LOG.debug("load private key");
        if (null == keyStoreResource && null == keyStoreFile)
            throw new RuntimeException("no key store resource or file set");
        if (null == keyStorePassword)
            throw new RuntimeException("no key store password set");
        if (null == keyStoreType)
            throw new RuntimeException("no key store type set");

        InputStream keyStoreInputStream;
        if (null != keyStoreResource) {
            Thread currenThread = Thread.currentThread();
            ClassLoader classLoader = currenThread.getContextClassLoader();
            keyStoreInputStream = classLoader.getResourceAsStream(keyStoreResource);
            if (null == keyStoreInputStream)
                throw new RuntimeException("keystore resource not found: " + keyStoreResource);
        } else {
            try {
                keyStoreInputStream = new FileInputStream(keyStoreFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("keystore file not found: " + keyStoreFile);
            }
        }

        PrivateKeyEntry privateKeyEntry;
        try {
            privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(keyStoreType, keyStoreInputStream, keyStorePassword,
                    keyStorePassword);
        } finally {
            IOUtils.closeQuietly(keyStoreInputStream);
        }
        privateKey = privateKeyEntry.getPrivateKey();
        certificate = (X509Certificate) privateKeyEntry.getCertificate();
        publicKey = certificate.getPublicKey();
        certificate = (X509Certificate) privateKeyEntry.getCertificate();
    }

    public void setKeyStorePassword(String keyStorePassword) {

        this.keyStorePassword = keyStorePassword;
    }

    public void setKeyStoreResource(String keyStoreResource) {

        LOG.debug("set key store resource: " + keyStoreResource);
        this.keyStoreResource = keyStoreResource;
    }

    public String getKeyStorePassword() {

        return keyStorePassword;
    }

    public String getKeyStoreResource() {

        LOG.debug("get key store resource: " + keyStoreResource);
        return keyStoreResource;
    }

    public String getKeyStoreType() {

        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {

        this.keyStoreType = keyStoreType;
    }

    public PrivateKey getPrivateKey() {

        if (null == privateKey) {
            loadKeyPair();
        }
        return privateKey;
    }

    public PublicKey getPublicKey() {

        if (null == publicKey) {
            loadKeyPair();
        }
        return publicKey;
    }

    public String getKeyStoreFile() {

        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {

        this.keyStoreFile = keyStoreFile;
    }

    public X509Certificate getCertificate() {

        if (null == certificate) {
            loadKeyPair();
        }
        return certificate;
    }
}
