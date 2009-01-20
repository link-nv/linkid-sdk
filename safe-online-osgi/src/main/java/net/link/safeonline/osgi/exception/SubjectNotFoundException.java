/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.exception;

/**
 * <h2>{@link SubjectNotFoundException}<br>
 * <sub>Attribute not found Exception.</sub></h2>
 * 
 * <p>
 * Exception thrown when a requested existing attribute could not be found for a
 * certain user by this OSGi plugin or by OLAS.
 * 
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SubjectNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String errorMessage;

	public SubjectNotFoundException(String errorMessage) {

		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {

		return errorMessage;
	}

}
