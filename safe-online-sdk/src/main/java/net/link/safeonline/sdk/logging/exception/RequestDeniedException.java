/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.logging.exception;

/**
 * Thrown in case the request has been denied by the service.
 * 
 * @author fcorneli
 */
public class RequestDeniedException extends Exception {

    public RequestDeniedException() {

    }

    public RequestDeniedException(String message) {

        super( message );
    }

}
