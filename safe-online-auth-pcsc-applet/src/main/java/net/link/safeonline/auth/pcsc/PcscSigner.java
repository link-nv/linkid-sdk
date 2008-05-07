/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

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

	private char[] getPin() {
		JLabel promptLabel = new JLabel("PIN:");

		JPasswordField passwordField = new JPasswordField(8);
		passwordField.setEchoChar('*');

		Box passwordPanel = Box.createHorizontalBox();
		passwordPanel.add(promptLabel);
		passwordPanel.add(Box.createHorizontalStrut(5));
		passwordPanel.add(passwordField);

		int result = JOptionPane.showOptionDialog(null, passwordPanel, "PIN",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if (result == JOptionPane.OK_OPTION) {
			char[] pin = passwordField.getPassword();
			return pin;
		}
		return null;
	}

	public byte[] sign(byte[] data) {
		String pin = new String(getPin());
		byte[] signatureValue;
		try {
			signatureValue = super.sign(data, pin);
		} catch (CardException e) {
			this.logger.log("card error: " + e.getMessage());
			throw new RuntimeException("card error: " + e.getMessage(), e);
		} catch (IOException e) {
			this.logger.log("IO error: " + e.getMessage());
			throw new RuntimeException("IO error: " + e.getMessage(), e);
		}
		return signatureValue;
	}
}
