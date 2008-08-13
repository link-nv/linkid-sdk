/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;


/**
 * JCE signer implementation.
 * 
 * @author fcorneli
 * 
 */
public class JceSigner implements Signer {

    private final PrivateKey      privateKey;

    private final X509Certificate certificate;


    public JceSigner(PrivateKey privateKey, X509Certificate certificate) {

        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public byte[] sign(byte[] data) {

        Signature signature;
        try {
            signature = Signature.getInstance("SHA1withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1withRSA algo not available");
        }
        try {
            signature.initSign(this.privateKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("invalid key: " + e.getMessage());
        }
        byte[] signatureValue;
        try {
            signature.update(data);
            signatureValue = signature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException("signature error: " + e.getMessage());
        }
        return signatureValue;
    }

    public X509Certificate getCertificate() {

        return this.certificate;
    }
}
