/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class DERBitString implements DEREncodable {

    public static final int BIT_STRING = 0x03;

    private final byte[]    data;


    public DERBitString(byte[] data) {

        this.data = data;
    }

    public byte[] getEncoded() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(BIT_STRING);
        try {
            DERUtils.writeLength(data.length + 1, out);
            out.write(0);
            out.write(data);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
        return out.toByteArray();
    }
}
