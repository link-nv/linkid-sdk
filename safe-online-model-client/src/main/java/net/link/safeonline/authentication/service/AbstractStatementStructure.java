/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.shared.asn1.statement.AbstractDERStatement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;


abstract public class AbstractStatementStructure {

    private static final Log LOG = LogFactory.getLog(AbstractStatementStructure.class);

    private final byte[]     signature;

    private final byte[]     toBeSignedData;


    public AbstractStatementStructure(byte[] encodedStatement) throws DecodingException {

        ASN1Sequence sequence;
        try {
            sequence = ASN1Sequence.getInstance(ASN1Object.fromByteArray(encodedStatement));
        } catch (IOException e) {
            LOG.debug("identity statement IO error: " + e.getMessage(), e);
            throw new DecodingException();
        }

        if (null == sequence) {
            LOG.debug("sequence is null");
            throw new DecodingException();
        }

        if (sequence.size() != 2) {
            LOG.error("sequence size: " + sequence.size());
            throw new DecodingException();
        }
        ASN1Sequence tbsSequence = ASN1Sequence.getInstance(sequence.getObjectAt(AbstractDERStatement.TBS_IDX));

        this.toBeSignedData = tbsSequence.getDEREncoded();

        decode(tbsSequence);

        DERBitString derSignature = DERBitString.getInstance(sequence.getObjectAt(AbstractDERStatement.SIGNATURE_IDX));
        this.signature = derSignature.getBytes();
    }

    protected abstract void decode(ASN1Sequence tbsSequence)
            throws DecodingException;

    protected abstract X509Certificate getCertificate();

    public byte[] getSignature() {

        return this.signature;
    }

    public byte[] getToBeSignedData() {

        return this.toBeSignedData;
    }

    protected X509Certificate decodeCertificate(byte[] certificate)
            throws DecodingException {

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(certificate);
            X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            return x509Certificate;
        } catch (CertificateException e) {
            LOG.debug("certificate exception: " + e.getMessage(), e);
            throw new DecodingException();
        }
    }
}
