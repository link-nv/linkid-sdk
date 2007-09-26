/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.shared.SharedConstants;

@ApplicationException(rollback = true)
public class SafeOnlineException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String errorCode;

	/**
	 * Gives back the error code. The error code is a unique code per exception
	 * type. It can be used by client-side components for better error handling.
	 * 
	 * @return
	 */
	public String getErrorCode() {
		return this.errorCode;
	}

	public SafeOnlineException() {
		this(null, SharedConstants.UNDEFINED_ERROR);
	}

	public SafeOnlineException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
