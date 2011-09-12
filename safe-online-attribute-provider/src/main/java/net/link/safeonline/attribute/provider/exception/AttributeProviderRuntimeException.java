/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.exception;

import org.jetbrains.annotations.Nullable;


/**
 * Attribute provider runtime exception. Case something unexpected happened.
 * <p/>
 * The exception is not a subclass of {@link RuntimeException} to avoid the annoying EJB EJBTransactionRolledbackException.
 */
public class AttributeProviderRuntimeException extends AbstractAttributeProviderException {

    public AttributeProviderRuntimeException(Throwable cause) {

        super( cause );
    }

    public AttributeProviderRuntimeException(String message) {

        super( message );
    }

    public AttributeProviderRuntimeException(String message, @Nullable Throwable cause) {

        super( message, cause );
    }
}
