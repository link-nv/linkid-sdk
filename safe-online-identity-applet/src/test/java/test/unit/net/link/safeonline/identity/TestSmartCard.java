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

public class TestSmartCard implements SmartCard {

	public void close() {
	}

	public X509Certificate getAuthenticationCertificate() {
		return null;
	}

	public PrivateKey getAuthenticationPrivateKey() {
		return null;
	}

	public String getCity() {
		return null;
	}

	public String getCountryCode() {
		return null;
	}

	public String getGivenName() {
		return null;
	}

	public String getPostalCode() {
		return null;
	}

	public X509Certificate getSignatureCertificate() {
		return null;
	}

	public PrivateKey getSignaturePrivateKey() {
		return null;
	}

	public String getStreet() {
		return null;
	}

	public String getSurname() {
		return null;
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
