/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.exception;

public class AttributeNotFoundException extends Exception {

    private final String attribute;

    public AttributeNotFoundException(final String attribute) {

        this.attribute = attribute;
    }

    public AttributeNotFoundException(final String attribute, final Throwable cause) {

        super( cause );
        this.attribute = attribute;
    }

    public String getAttribute() {

        return attribute;
    }
}
