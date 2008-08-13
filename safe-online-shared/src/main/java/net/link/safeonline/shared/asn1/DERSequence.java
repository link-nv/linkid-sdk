/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class DERSequence implements DEREncodable {

    public static final int          SEQUENCE    = 0x10;

    public static final int          CONSTRUCTED = 0x20;

    private final List<DEREncodable> sequence;


    public DERSequence(List<DEREncodable> sequence) {

        this.sequence = sequence;
    }

    public DERSequence() {

        this(new LinkedList<DEREncodable>());
    }

    public void add(DEREncodable element) {

        this.sequence.add(element);
    }

    public byte[] getEncoded() {

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (DEREncodable derEncodable : this.sequence) {
            try {
                body.write(derEncodable.getEncoded());
            } catch (IOException e) {
                throw new RuntimeException("IO error: " + e.getMessage(), e);
            }
        }
        byte[] bodyData = body.toByteArray();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(SEQUENCE | CONSTRUCTED);
        try {
            DERUtils.writeLength(bodyData.length, out);
            out.write(bodyData);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }
}
