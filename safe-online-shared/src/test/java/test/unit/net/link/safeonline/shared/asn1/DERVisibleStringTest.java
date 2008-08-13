/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.asn1;

import junit.framework.TestCase;
import net.link.safeonline.shared.asn1.DERVisibleString;

import org.bouncycastle.asn1.ASN1Object;


public class DERVisibleStringTest extends TestCase {

    public void testDERVisible() throws Exception {

        // setup
        String value = "hello world";
        DERVisibleString visibleString = new DERVisibleString(value);

        // operate
        byte[] result = visibleString.getEncoded();

        // verify
        assertNotNull(result);
        org.bouncycastle.asn1.DERVisibleString resultString = (org.bouncycastle.asn1.DERVisibleString) ASN1Object
                .fromByteArray(result);
        assertEquals(value, resultString.getString());
    }
}
