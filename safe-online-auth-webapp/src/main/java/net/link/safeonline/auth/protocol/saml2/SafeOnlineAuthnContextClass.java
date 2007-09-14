/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol.saml2;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of authentication context classes supported by SafeOnline.
 * 
 * @author fcorneli
 * 
 */
public enum SafeOnlineAuthnContextClass {

	PASSWORD_PROTECTED_TRANSPORT(
			"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport"), SMART_CARD_PKI(
			"urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI");

	private final String samlName;

	private SafeOnlineAuthnContextClass(String samlName) {
		this.samlName = samlName;
	}

	public String getSamlName() {
		return this.samlName;
	}

	private static final Map<String, SafeOnlineAuthnContextClass> samlNames = new HashMap<String, SafeOnlineAuthnContextClass>();

	static {
		SafeOnlineAuthnContextClass[] authnContextClasses = SafeOnlineAuthnContextClass
				.values();
		for (SafeOnlineAuthnContextClass authnContextClass : authnContextClasses) {
			samlNames.put(authnContextClass.getSamlName(), authnContextClass);
		}
	}

	public static SafeOnlineAuthnContextClass findAuthnContextClass(
			String samlName) {
		SafeOnlineAuthnContextClass authnContextClass = samlNames.get(samlName);
		return authnContextClass;
	}
}
