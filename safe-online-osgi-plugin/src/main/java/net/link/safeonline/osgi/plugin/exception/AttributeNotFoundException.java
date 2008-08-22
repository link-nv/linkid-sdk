/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.plugin.exception;

import net.link.safeonline.osgi.plugin.Attribute;

/**
 * <h2>{@link AttributeNotFoundException}<br>
 * <sub>Unsupported Data Type Exception.</sub></h2>
 * 
 * <p>
 * Exception thrown while trying to set an {@link Attribute} value with an
 * unknown data type.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AttributeNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String errorMessage;

	public AttributeNotFoundException(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

}
