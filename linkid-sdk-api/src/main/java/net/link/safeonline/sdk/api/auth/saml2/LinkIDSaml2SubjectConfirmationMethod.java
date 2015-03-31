/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth.saml2;

/**
 * <h2>{@link LinkIDSaml2SubjectConfirmationMethod}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Dec 17, 2008</i>
 * </p>
 *
 * @author wvdhaute
 */
public enum LinkIDSaml2SubjectConfirmationMethod {

    HOLDER_OF_KEY( "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key" ),
    SENDER_VOUCHES( "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches" ),
    BEARER( "urn:oasis:names:tc:SAML:2.0:cm:bearer" );

    private final String methodURI;

    private LinkIDSaml2SubjectConfirmationMethod(String methodURI) {

        this.methodURI = methodURI;
    }

    public String getMethodURI() {

        return methodURI;
    }
}
