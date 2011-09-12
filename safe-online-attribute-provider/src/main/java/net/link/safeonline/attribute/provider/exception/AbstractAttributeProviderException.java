/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.exception;

import org.jetbrains.annotations.Nullable;


public abstract class AbstractAttributeProviderException extends Exception {

    protected boolean logError = true;

    protected AbstractAttributeProviderException(Throwable cause) {

        this( cause.getMessage(), cause );
    }

    protected AbstractAttributeProviderException(String message) {

        this( message, null );
    }

    protected AbstractAttributeProviderException(String message, @Nullable Throwable cause) {

        super( message, cause );
    }

    public void markLogged() {

        logError = false;
    }

    public boolean isLogged() {

        return !logError;
    }
}
