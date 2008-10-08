/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import javax.servlet.UnavailableException;


/**
 * Enumeration of all supported authentication protocols.
 *
 * @author fcorneli
 *
 */
public enum AuthenticationProtocol {

    SAML2_BROWSER_POST, UNIT_TEST;

    public static AuthenticationProtocol toAuthenticationProtocol(String value) throws UnavailableException {

        try {
            AuthenticationProtocol authenticationProtocol = AuthenticationProtocol.valueOf(value);
            return authenticationProtocol;
        } catch (IllegalArgumentException e) {
            throw new UnavailableException("invalid authentication protocol: " + value);
        }
    }
}
