/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DemoApplet extends JApplet {

	private static final long serialVersionUID = 1L;

	public DemoApplet() {
		JTextArea outputArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(outputArea);
		add(scrollPane);

		// /usr/share/pcsc/smartcard_list.txt
		List<byte[]> supportedATRs = new LinkedList<byte[]>();
		supportedATRs.add(new byte[] { 0x3B, (byte) 0x98, (byte) 0x94, 0x40,
				0x0A, (byte) 0xA5, 0x03, 0x01, 0x01, 0x01, (byte) 0xAD, 0x13,
				0x10 });

		// eID release 1.2
		// byte 3 (value 0x13): PPS value: 13 relates to the communication speed
		// byte 13 (value 0X11): Applet version 1.1
		supportedATRs.add(new byte[] { 0x3B, (byte) 0x98, 0x13, 0x40, 0x0A,
				(byte) 0xA5, 0x03, 0x01, 0x01, 0x01, (byte) 0xAD, 0x13, 0x11 });

		outputArea.append("Hello World\n");

		TerminalFactory terminalFactory = TerminalFactory.getDefault();

		CardTerminals cardTerminals = terminalFactory.terminals();

		List<CardTerminal> terminals;
		try {
			terminals = cardTerminals.list();
		} catch (CardException e) {
			outputArea.append("card exception: " + e.getMessage() + "\n");
			return;
		}

		outputArea.append("# card terminals: " + terminals.size() + "\n");

		for (CardTerminal terminal : terminals) {
			outputArea.append("terminal name: " + terminal.getName() + "\n");
			try {
				outputArea.append("card present: " + terminal.isCardPresent()
						+ "\n");
			} catch (CardException e) {
				outputArea.append("card exception: " + e.getMessage() + "\n ");
				return;
			}

			Card card;
			try {
				card = terminal.connect("*");
			} catch (CardException e) {
				outputArea.append("card exception: " + e.getMessage() + "\n ");
				return;
			} catch (Exception e) {
				outputArea.append("exception: " + e.getMessage() + "\n ");
				return;
			}
			outputArea.append("card protocol: " + card.getProtocol() + "\n");

			ATR atr = card.getATR();

			byte[] atrBytes = atr.getBytes();
			outputArea.append("ATR: " + toHexString(atrBytes) + "\n");

			for (byte[] supportedATR : supportedATRs) {
				if (Arrays.equals(supportedATR, atrBytes)) {
					outputArea.append("eID card detected\n");
				}
			}

			CardChannel cardChannel = card.getBasicChannel();

			CommandAPDU selectIdentityFile = new CommandAPDU(0x00, 0xa4, 0x08,
					0x0c, new byte[] { 0x3f, 0x00, (byte) 0xdf, 0x01, 0x40,
							(byte) 0x31 });
			ResponseAPDU responseApdu;
			try {
				responseApdu = cardChannel.transmit(selectIdentityFile);
			} catch (CardException e) {
				outputArea.append("card exception: " + e.getMessage() + "\n ");
				return;
			}

			CommandAPDU getFileApdu = new CommandAPDU(0x00, 0xb0, 0x00, 0x00,
					0xf0);
			try {
				responseApdu = cardChannel.transmit(getFileApdu);
			} catch (CardException e) {
				outputArea.append("card exception: " + e.getMessage() + "\n ");
				return;
			}

			BeIDIdentity beIDIdentity = new BeIDIdentity(responseApdu.getData());
			outputArea.append("name: " + beIDIdentity.getName() + "\n");
			outputArea.append("given names: " + beIDIdentity.getGivenNames()
					+ "\n");
			outputArea.append("national number: "
					+ beIDIdentity.getNationalNumber() + "\n");

			try {
				card.disconnect(true);
			} catch (CardException e) {
				outputArea.append("card exception: " + e.getMessage() + "\n ");
				return;
			}
		}
	}

	public class DataParser {
		private byte[] data;

		public DataParser(byte[] data) {
			this.data = data;
		}

		protected String getField(int fieldIdx) {
			int idx = 0;
			while (fieldIdx > 0) {
				int length = this.data[idx + 1];
				idx += length + 2;
				fieldIdx--;
			}
			idx++;
			int length = this.data[idx];
			String result = new String(Arrays.copyOfRange(this.data, idx + 1,
					idx + 1 + length));
			return result;
		}
	}

	public class BeIDIdentity extends DataParser {

		public BeIDIdentity(byte[] data) {
			super(data);
		}

		public String getCardNumber() {
			return getField(0);
		}

		public String getValidFrom() {
			return getField(2);
		}

		public String getValidUntil() {
			return getField(3);
		}

		public String getIssuingMunicipality() {
			return getField(4);
		}

		public String getNationalNumber() {
			return getField(5);
		}

		public String getName() {
			return getField(6);
		}

		public String getGivenNames() {
			return getField(7);
		}

		public String getMiddleName() {
			return getField(8);
		}

		public String getNationality() {
			return getField(9);
		}

		public String getPlaceOfBirth() {
			return getField(10);
		}

		public String getDateOfBirth() {
			return getField(11);
		}

		public String getSex() {
			return getField(12);
		}
	}

	private String toHexString(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (byte b : bytes) {
			result.append(Integer.toHexString(b & 0xff));
		}
		return result.toString();
	}
}
