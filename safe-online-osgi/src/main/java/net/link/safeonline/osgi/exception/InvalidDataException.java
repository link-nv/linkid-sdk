/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.exception;

/**
 * <h2>{@link InvalidDataException}<br>
 * <sub>Unsupported Data Type Exception.</sub></h2>
 * 
 * <p>
 * Exception thrown by OLAS when an unexpected data type is found for a certain
 * attribute type.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class InvalidDataException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String errorMessage;

	public InvalidDataException(String errorMessage) {

		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {

		return errorMessage;
	}

}
