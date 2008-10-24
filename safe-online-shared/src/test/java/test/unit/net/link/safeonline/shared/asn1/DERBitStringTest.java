/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.asn1;

import java.util.Arrays;

import junit.framework.TestCase;
import net.link.safeonline.shared.asn1.DERBitString;

import org.bouncycastle.asn1.ASN1Object;


public class DERBitStringTest extends TestCase {

    public void testEncoding() throws Exception {

        // setup
        byte[] data = "hello world".getBytes();

        // operate
        DERBitString bitString = new DERBitString(data);
        byte[] result = bitString.getEncoded();

        // verify
        assertNotNull(result);
        org.bouncycastle.asn1.DERBitString resultBitString = org.bouncycastle.asn1.DERBitString
                                                                                               .getInstance(ASN1Object
                                                                                                                      .fromByteArray(result));
        assertTrue(Arrays.equals(data, resultBitString.getBytes()));
    }
}
