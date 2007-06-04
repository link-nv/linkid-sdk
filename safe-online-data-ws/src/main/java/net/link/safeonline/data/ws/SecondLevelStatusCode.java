/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerate of ID-WSF DST v2.1 Second Level Status Codes.
 * 
 * <p>
 * Specification: ID-WSF DST v2.1 - 3.2.2. Second Level Status Codes
 * </p>
 * 
 * @author fcorneli
 * 
 */
public enum SecondLevelStatusCode {
	NOT_AUTHORIZED("ActionNotAuthorized"), UNSUPPORTED_OBJECT_TYPE(
			"UnsupportedObjectType"), NO_MULTIPLE_ALLOWED("NoMultipleAllowed"), PAGINATION_NOT_SUPPORTED(
			"PaginationNotSupported"), DOES_NOT_EXIST("DoesNotExist"), MISSING_OBJECT_TYPE(
			"MissingObjectType"), INVALID_DATA("InvalidData"), EMPTY_REQUEST(
			"EmptyRequest"), MISSING_SELECT("MissingSelect"), MISSING_CREDENTIALS(
			"MissingCredentials");

	private String code;

	private static Map<String, SecondLevelStatusCode> statusCodes = new HashMap<String, SecondLevelStatusCode>();

	static {
		for (SecondLevelStatusCode secondLevelStatusCode : SecondLevelStatusCode
				.values()) {
			SecondLevelStatusCode.statusCodes.put(secondLevelStatusCode
					.getCode(), secondLevelStatusCode);
		}
	}

	private SecondLevelStatusCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public static SecondLevelStatusCode fromCode(String code) {
		SecondLevelStatusCode secondLevelStatusCode = SecondLevelStatusCode.statusCodes
				.get(code);
		if (null == secondLevelStatusCode) {
			throw new IllegalArgumentException("unknown code: " + code);
		}
		return secondLevelStatusCode;
	}
}