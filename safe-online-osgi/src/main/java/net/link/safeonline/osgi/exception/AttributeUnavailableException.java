/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.exception;

/**
 * <h2>{@link AttributeUnavailableException}<br>
 * <sub>Attribute Unavailable Exception.</sub></h2>
 * 
 * <p>
 * Exception thrown when a requested attribute is not available. Reasons can be that the OSGi plugin responsible for this type is not
 * running.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AttributeUnavailableException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String      errorMessage;


    public AttributeUnavailableException(String errorMessage) {

        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {

        return this.errorMessage;
    }

}
