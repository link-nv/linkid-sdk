/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1;

public class DEREncodedData implements DEREncodable {

    private final byte[] data;


    public DEREncodedData(byte[] data) {

        this.data = data;
    }

    public byte[] getEncoded() {

        return data;
    }
}
