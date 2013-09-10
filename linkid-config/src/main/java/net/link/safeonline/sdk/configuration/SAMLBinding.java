/*
     * SafeOnline project.
     *
     * Copyright 2006-2009 Lin.k N.V. All rights reserved.
     * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
     */
package net.link.safeonline.sdk.configuration;

/**
 * <h2>{@link SAMLBinding}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Apr 17, 2009</i> </p>
 *
 * @author lhunath
 */
public enum SAMLBinding {

    HTTP_POST( "SAML HTTP POST" ),
    HTTP_REDIRECT( "SAML HTTP Redirect" );

    String name;

    SAMLBinding(String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        return name;
    }
}
