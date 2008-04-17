/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.security.cert.X509Certificate;

import javax.smartcardio.CardChannel;

import net.link.safeonline.shared.Signer;

public class PcscSigner extends Pcsc implements Signer {

	private final PcscSignerLogger logger;

	public PcscSigner(CardChannel cardChannel, PcscSignerLogger logger) {
		super(cardChannel);
		if (null == logger) {
			this.logger = new NullPcscSignerLogger();
		} else {
			this.logger = logger;
		}
	}

	public X509Certificate getCertificate() {
		try {
			return this.getAuthenticationCertificate();
		} catch (Exception e) {
			this.logger.log("error: " + e.getMessage());
			throw new RuntimeException("error");
		}
	}

	public byte[] sign(byte[] data) {
		// TODO implement me
		return null;
	}
}
