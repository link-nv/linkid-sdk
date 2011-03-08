/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.protocol.saml2;

public class UnknownStatusException extends Exception {

    private String statusCode;

    public UnknownStatusException(String statusCode) {

        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {

        return "Unknown status: " + statusCode;
    }
}
