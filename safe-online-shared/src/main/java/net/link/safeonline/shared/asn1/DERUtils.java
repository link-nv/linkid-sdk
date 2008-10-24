/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1;

import java.io.IOException;
import java.io.OutputStream;


public class DERUtils {

    private DERUtils() {

        // empty
    }

    /**
     * Write the length of the byte sequence.
     * 
     * Source: org.bouncycastle.asn1.DEROutputStream.writeLength
     * 
     * @param length
     * @param out
     * @throws IOException
     */
    public static void writeLength(int length, OutputStream out) throws IOException {

        if (length > 127) {
            int size = 1;
            int val = length;

            while ((val >>>= 8) != 0) {
                size++;
            }

            out.write((byte) (size | 0x80));

            for (int i = (size - 1) * 8; i >= 0; i -= 8) {
                out.write((byte) (length >> i));
            }
        } else {
            out.write((byte) length);
        }
    }
}
