/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attrib.ws;

import java.util.HashMap;
import java.util.Map;

/**
 * SAMLp version 2.0 Second-Level Error Code.
 * 
 * <p>
 * Specification: 3.2.2.2 Element StatusCode - Assertions and Protocols for the
 * OASIS Security Assertion Markup Language (SAML) V2.0 - OASIS Standard, 15
 * March 2005
 * </p>
 * 
 * @author fcorneli
 * 
 */
public enum SamlpSecondLevelErrorCode {
	INVALID_ATTRIBUTE_NAME_OR_VALUE(
			"urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue"), UNKNOWN_PRINCIPAL(
			"urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal"), REQUEST_DENIED(
			"urn:oasis:names:tc:SAML:2.0:status:RequestDenied");

	private final String errorCode;

	private final static Map<String, SamlpSecondLevelErrorCode> errorCodeMap = new HashMap<String, SamlpSecondLevelErrorCode>();

	static {
		SamlpSecondLevelErrorCode[] errorCodes = SamlpSecondLevelErrorCode
				.values();
		for (SamlpSecondLevelErrorCode errorCode : errorCodes) {
			errorCodeMap.put(errorCode.getErrorCode(), errorCode);
		}
	}

	private SamlpSecondLevelErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public static SamlpSecondLevelErrorCode getSamlpTopLevelErrorCode(
			String errorCode) {
		SamlpSecondLevelErrorCode samlpSecondLevelErrorCode = errorCodeMap
				.get(errorCode);
		if (null == samlpSecondLevelErrorCode) {
			throw new IllegalArgumentException(
					"unknown SAMLp second-level error code: " + errorCode);
		}
		return samlpSecondLevelErrorCode;
	}
}