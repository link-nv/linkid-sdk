/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.logging.exception;

/**
 * Thrown in case STS validation failed.
 *
 * @author wvdhaute
 */
public class ValidationFailedException extends Exception {

    public ValidationFailedException(String message) {
        super( message );
    }

    public ValidationFailedException(final Throwable throwable) {
        super( throwable );
    }
}
