/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class DERInteger implements DEREncodable {

	public static final int INTEGER = 0x02;

	private final byte[] bytes;

	public DERInteger(int value) {
		this.bytes = BigInteger.valueOf(value).toByteArray();
	}

	public byte[] getEncoded() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(INTEGER);
		try {
			DERUtils.writeLength(this.bytes.length, out);
			out.write(this.bytes);
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage());
		}
		return out.toByteArray();
	}
}
