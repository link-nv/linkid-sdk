/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.asn1;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.shared.asn1.DEREncodable;
import net.link.safeonline.shared.asn1.DERInteger;
import net.link.safeonline.shared.asn1.DERSequence;

import org.bouncycastle.asn1.ASN1Object;


public class DERSequenceTest extends TestCase {

    public void testDERSequence() throws Exception {

        // setup
        List<DEREncodable> sequenceList = new LinkedList<DEREncodable>();
        int value1 = 1234;
        int value2 = 5678;
        DERInteger int1 = new DERInteger(value1);
        DERInteger int2 = new DERInteger(value2);
        sequenceList.add(int1);
        sequenceList.add(int2);

        // operate
        DERSequence sequence = new DERSequence(sequenceList);
        byte[] result = sequence.getEncoded();

        // verify
        assertNotNull(result);
        org.bouncycastle.asn1.DERSequence resultSequence = (org.bouncycastle.asn1.DERSequence) ASN1Object.fromByteArray(result);
        assertEquals(sequenceList.size(), resultSequence.size());
        org.bouncycastle.asn1.DERInteger resultInt1 = (org.bouncycastle.asn1.DERInteger) resultSequence.getObjectAt(0);
        org.bouncycastle.asn1.DERInteger resultInt2 = (org.bouncycastle.asn1.DERInteger) resultSequence.getObjectAt(1);
        assertEquals(value1, resultInt1.getValue().intValue());
        assertEquals(value2, resultInt2.getValue().intValue());
    }
}
