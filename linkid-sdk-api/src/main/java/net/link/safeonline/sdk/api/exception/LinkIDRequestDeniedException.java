/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.exception;

/**
 * Thrown in case the request has been denied by the service.
 *
 * @author fcorneli
 */
public class LinkIDRequestDeniedException extends Exception {

    public LinkIDRequestDeniedException() {

    }

    public LinkIDRequestDeniedException(String message) {

        super( message );
    }
}
