/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1.statement;

import net.link.safeonline.shared.asn1.DERBitString;
import net.link.safeonline.shared.asn1.DEREncodable;
import net.link.safeonline.shared.asn1.DERSequence;


public abstract class AbstractDERStatement implements DEREncodable {

    public static final int TBS_IDX       = 0;

    public static final int SIGNATURE_IDX = 1;

    private byte[]          signature;


    protected abstract DEREncodable getToBeSigned();

    public void setSignature(byte[] signature) {

        this.signature = signature;
    }

    public byte[] getEncoded() {

        DEREncodable tbs = getToBeSigned();

        DERSequence sequence = new DERSequence();
        sequence.add(tbs);
        if (null == signature)
            throw new IllegalStateException("set signature value first");
        DERBitString signatureBitString = new DERBitString(signature);
        sequence.add(signatureBitString);
        return sequence.getEncoded();
    }

    public byte[] getToBeSignedEncoded() {

        DEREncodable tbs = getToBeSigned();
        byte[] encoded = tbs.getEncoded();
        return encoded;
    }
}
