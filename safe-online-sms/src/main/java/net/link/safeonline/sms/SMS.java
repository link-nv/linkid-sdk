/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms;

import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMS {

	static Log LOG = LogFactory.getLog(SMS.class);

	String from = null;

	String to = null;

	String message = null;

	public SMS() {
		// empty
	}

	public SMS(String to, String message) {
		this.to = strip(to);
		this.message = message;
	}

	public String getFrom() {
		return this.from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return this.to;
	}

	public void setTo(String to) {
		this.to = strip(to);
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public byte[] getEncoded() {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		try {
			ba.write(0x00); // length of SMSC info
			ba.write(0x01); // this is a SMS-SUBMIT message
			ba.write(0x00); // message reference
			ba.write(this.to.length()); // telephone number length
			ba.write(0x91); // indicates an international phone number
			ba.write(telToBytes(this.to)); // telephone number
			ba.write(0x00); // protocol identifier
			ba.write(0x00); // Data coding scheme
			ba.write(this.message.length()); // length of message
			ba.write(msgToBytes(this.message)); // message
		} catch (Exception e) {
			LOG.debug("Exception while creating SMS encoding");
			LOG.debug(e.getMessage());
			LOG.debug(e.getStackTrace());
		}
		return ba.toByteArray();
	}

	/**
	 * strips all non-numbers from the telephone numbers
	 * 
	 * @param telnr
	 */
	public static String strip(String telnr) {
		return telnr.replaceAll("[^0-9]", "");
	}

	/**
	 * converts a telephone number to the format needed in sms encoding
	 * communication
	 * 
	 * @param telnummer
	 */
	public static byte[] telToBytes(String telnummer) {
		String telnr = strip(telnummer);
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		for (int index = 0; index < telnr.length(); index++) {
			telnr.substring(index, index);
			Integer a = new Integer(telnr.substring(index, index + 1));
			index++;
			Integer b = null;
			if (index < telnr.length()) {
				b = new Integer(telnr.substring(index, index + 1));
			} else {
				b = new Integer(15);
			}
			byte total = new Integer(a.intValue() + (b.intValue() * 16))
					.byteValue();
			ba.write(total);
		}

		return ba.toByteArray();
	}

	/**
	 * converts a message to the sms encoding
	 * 
	 * @param message
	 * @throws Exception
	 */
	public static byte[] msgToBytes(String message) throws Exception {

		// convert the characters to their ASCII value
		byte[] orig = message.getBytes("US-ASCII");

		// reserve space for the encoded message
		byte[] res = new byte[((orig.length * 7) / 8) + 1];

		// fill each byte of the result
		for (int i = 0; i < res.length; i++) {
			// the position of the first of the 2 original bytes that will fill
			// this one encoded byte
			int origbytepos = (i * 8) / 7;

			// how much of each of those 2 original bytes is kept
			int padding = (i % 7);

			// the first byte
			Integer first = new Integer(orig[origbytepos]);
			// move it some positions to the right
			for (int div = padding; div > 0; div--) {
				first = first / 2;
			}

			Integer second;
			if (origbytepos + 1 == orig.length) {
				second = new Integer(0);
			} else {
				second = new Integer(orig[origbytepos + 1]);
			}

			// move the second byte some positions to the left
			for (int mul = 7 - padding; mul > 0; mul--) {
				second = second * 2;
			}

			// combine them
			Integer result = first + second;

			res[i] = result.byteValue();
		}

		return res;

	}

}
