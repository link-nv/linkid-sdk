/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


abstract public class AbstractStatement<T extends AbstractStatementStructure> {

    private static final Log LOG = LogFactory.getLog(AbstractStatement.class);

    private final T          statementStructure;


    public AbstractStatement(T statementStructure) {

        this.statementStructure = statementStructure;
    }

    protected T getStatementStructure() {

        return this.statementStructure;
    }

    /**
     * Verifies the integrity of the statement.
     * 
     */
    public X509Certificate verifyIntegrity() {

        X509Certificate authCert = this.statementStructure.getCertificate();
        byte[] data = this.statementStructure.getToBeSignedData();
        Signature signature;
        try {
            signature = Signature.getInstance("SHA1withRSA");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("sign algo error: " + e.getMessage(), e);
            return null;
        }
        try {
            signature.initVerify(authCert);
        } catch (InvalidKeyException e) {
            LOG.error("Invalid key: " + e.getMessage(), e);
            return null;
        }
        try {
            signature.update(data);
            boolean result = signature.verify(this.statementStructure.getSignature());
            if (result)
                return authCert;
            return null;
        } catch (SignatureException e) {
            LOG.error("signature error: " + e.getMessage());
            return null;
        }
    }
}
