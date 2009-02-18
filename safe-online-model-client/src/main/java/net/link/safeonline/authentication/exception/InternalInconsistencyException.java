/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.exception;

/**
 * <h2>{@link InternalInconsistencyException}<br>
 * <sub>Indicate a bug.</sub></h2>
 * 
 * <p>
 * When this exception is fired, something happened that under all expected circumstances should never happen. With other words, a BUG.
 * </p>
 * 
 * <p>
 * <i>Feb 16, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class InternalInconsistencyException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public InternalInconsistencyException(String bugDescription) {

        super(bugDescription);
    }

    public InternalInconsistencyException(String bugDescription, Throwable cause) {

        super(bugDescription, cause);
    }
}
