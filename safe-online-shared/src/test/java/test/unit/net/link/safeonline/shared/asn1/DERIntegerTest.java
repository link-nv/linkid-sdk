/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.asn1;

import junit.framework.TestCase;
import net.link.safeonline.shared.asn1.DERInteger;

import org.bouncycastle.asn1.ASN1Object;


public class DERIntegerTest extends TestCase {

    public void testDERInteger() throws Exception {

        // setup
        int value = 12345678;

        // operate
        DERInteger derInteger = new DERInteger(value);
        byte[] result = derInteger.getEncoded();

        // verify
        assertNotNull(result);
        org.bouncycastle.asn1.DERInteger resultInteger = (org.bouncycastle.asn1.DERInteger) ASN1Object.fromByteArray(result);
        assertEquals(value, resultInteger.getValue().intValue());
    }
}
