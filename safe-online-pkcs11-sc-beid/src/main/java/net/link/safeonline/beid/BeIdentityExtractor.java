/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import net.link.safeonline.p11sc.spi.IdentityDataCollector;
import net.link.safeonline.p11sc.spi.IdentityDataExtractor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeIdentityExtractor implements IdentityDataExtractor {

	private static final Log LOG = LogFactory.getLog(BeIdentityExtractor.class);

	private static Map<String, String> oidMap;

	private IdentityDataCollector identityDataCollector;

	static {
		oidMap = new HashMap<String, String>();
		oidMap.put("2.5.4.5", "SERIALNUMBER");
		oidMap.put("2.5.4.42", "GIVENNAME");
		oidMap.put("2.5.4.4", "SURNAME");
	}

	public void init(IdentityDataCollector identityDataCollector) {
		LOG.debug("init");
		this.identityDataCollector = identityDataCollector;
	}

	public void postPkcs11(X509Certificate authenticationCertificate) {
		LOG.debug("postPkcs11");
		String subjectName = getSubjectName(authenticationCertificate);
		LOG.debug("subject: " + subjectName);

		String givenName = getAttributeFromSubjectName(subjectName, "GIVENNAME");
		this.identityDataCollector.setGivenName(givenName);

		String surname = getAttributeFromSubjectName(subjectName, "SURNAME");
		this.identityDataCollector.setSurname(surname);

		String countryCode = getAttributeFromSubjectName(subjectName, "C");
		this.identityDataCollector.setCountryCode(countryCode);
	}

	private String getSubjectName(X509Certificate certificate) {
		X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
		String subjectName = subjectPrincipal.getName(X500Principal.RFC1779,
				oidMap);
		return subjectName;
	}

	private String getAttributeFromSubjectName(String subjectName,
			String attributeName) {
		int attributeBegin = subjectName.indexOf(attributeName + "=");
		if (-1 == attributeBegin) {
			throw new IllegalArgumentException(
					"attribute name does not occur in subject: "
							+ attributeName);
		}
		attributeBegin += attributeName.length() + 1; // "attributeName="
		int attributeEnd = subjectName.indexOf(",", attributeBegin);
		if (-1 == attributeEnd) {
			// last field has no trailing ","
			attributeEnd = subjectName.length();
		}
		String attributeValue = subjectName.substring(attributeBegin,
				attributeEnd);
		return attributeValue;
	}

	public void prePkcs11(CardTerminal cardTerminal) {
		LOG.debug("prePkcs11");

		try {
			Card card = cardTerminal.connect("*");
			try {
				CardChannel cardChannel = card.getBasicChannel();
				/*
				 * TODO: correctly parse the PKCS#15 data structure.
				 */
				CommandAPDU selectAddressFile = new CommandAPDU(0x00, 0xa4,
						0x08, 0x0c, new byte[] { 0x3f, 0x00, (byte) 0xdf, 0x01,
								0x40, (byte) 0x33 });
				ResponseAPDU responseApdu = cardChannel
						.transmit(selectAddressFile);
				if (responseApdu.getSW() != 0x9000) {
					return;
				}

				CommandAPDU getFileApdu = new CommandAPDU(0x00, 0xb0, 0x00,
						0x00, 0xf0);
				responseApdu = cardChannel.transmit(getFileApdu);
				if (responseApdu.getSW() != 0x9000) {
					return;
				}
				BeIDAddress address = new BeIDAddress(responseApdu.getData());
				LOG.debug("street: \"" + address.getStreet() + "\"");
				LOG.debug("postcode: \"" + address.getPostcode() + "\"");
				LOG.debug("city: \"" + address.getCity() + "\"");
				this.identityDataCollector.setStreet(address.getStreet());
				this.identityDataCollector.setCity(address.getCity());
				this.identityDataCollector.setPostalCode(address.getPostcode());
			} finally {
				card.disconnect(true);
			}
		} catch (CardException e) {
			LOG.error("card connect error: " + e.getMessage());
		}
	}

	/**
	 * The format of the data is like:
	 * 
	 * byte 0: sequence nr: 1, 2, 3 where 1 = street, 2 = postcode, 3 = city
	 * 
	 * byte 1: size of the string
	 * 
	 * byte 2-n: the string
	 * 
	 * @author fcorneli
	 * 
	 */
	public class BeIDAddress extends DataParser {

		public BeIDAddress(byte[] data) {
			super(data);
		}

		public String getStreet() {
			return getField(0);
		}

		public String getPostcode() {
			return getField(1);
		}

		public String getCity() {
			return getField(2);
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
}
