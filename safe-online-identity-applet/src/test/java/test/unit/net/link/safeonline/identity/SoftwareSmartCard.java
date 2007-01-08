/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.identity;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardPinCallback;

public class SoftwareSmartCard implements SmartCard {

	private final String givenName;

	private final String surname;

	private final String street;

	private final String postalCode;

	private final String city;

	private final PrivateKey authenticationPrivateKey;

	private final X509Certificate authenticationCertificate;

	public SoftwareSmartCard(final String givenName, final String surname,
			final String street, final String postalCode, final String city,
			final PrivateKey authenticationPrivateKey,
			final X509Certificate authenticationCertificate) {
		this.givenName = givenName;
		this.surname = surname;
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
		this.authenticationPrivateKey = authenticationPrivateKey;
		this.authenticationCertificate = authenticationCertificate;
	}

	public void close() {
	}

	public X509Certificate getAuthenticationCertificate() {
		return this.authenticationCertificate;
	}

	public PrivateKey getAuthenticationPrivateKey() {
		return this.authenticationPrivateKey;
	}

	public String getCity() {
		return this.city;
	}

	public String getCountryCode() {
		return null;
	}

	public String getGivenName() {
		return this.givenName;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public X509Certificate getSignatureCertificate() {
		return null;
	}

	public PrivateKey getSignaturePrivateKey() {
		return null;
	}

	public String getStreet() {
		return this.street;
	}

	public String getSurname() {
		return this.surname;
	}

	public void init(List<SmartCardConfig> smartCardConfigs) {
	}

	public boolean isOpen() {
		return false;
	}

	public boolean isReaderPresent() {
		return false;
	}

	public boolean isSupportedCardPresent() {
		return false;
	}

	public void open() {
	}

	public void setSmartCardPinCallback(
			SmartCardPinCallback smartCardPinCallback) {
	}
}
