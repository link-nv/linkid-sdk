/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class DERVisibleString implements DEREncodable {

    public static final int VISIBLE_STRING = 0x1a;

    public final String     value;


    public DERVisibleString(String value) {

        this.value = value;
    }

    public byte[] getEncoded() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(VISIBLE_STRING);
        byte[] octets = getOctets();
        try {
            DERUtils.writeLength(octets.length, outputStream);
            outputStream.write(octets);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        }
        return outputStream.toByteArray();
    }

    public byte[] getOctets() {

        char[] cs = this.value.toCharArray();
        byte[] bs = new byte[cs.length];

        for (int i = 0; i != cs.length; i++) {
            bs[i] = (byte) cs[i];
        }

        return bs;
    }
}
