/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.exception;

/**
 * <h2>{@link AttributeTypeNotFoundException}<br>
 * <sub>Attribute type not found Exception.</sub></h2>
 * 
 * <p>
 * Exception thrown when a requested attribute's type does not exist in the OSGi
 * plugin or OLAS.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AttributeTypeNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String errorMessage;

	public AttributeTypeNotFoundException(String errorMessage) {

		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {

		return errorMessage;
	}

}
