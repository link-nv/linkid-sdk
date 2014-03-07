/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import java.security.cert.X509Certificate;
import net.link.util.common.CertificateChain;
import org.opensaml.saml2.core.StatusResponseType;


/**
 * Holds a SAML {@link StatusResponseType} object and the {@link X509Certificate} chain embedded in the Signature on the response if any.
 * If
 * Binding was HTTP-Redirect the chain is <code>null</code>
 *
 * @author Wim Vandenhaute
 */
public class Saml2ResponseContext {

    private final StatusResponseType response;
    private final CertificateChain   certificateChain;

    public Saml2ResponseContext(final StatusResponseType response, final CertificateChain certificateChain) {

        this.response = response;
        this.certificateChain = certificateChain;
    }

    /**
     * @return the SAML v2.0 response
     */
    public StatusResponseType getResponse() {

        return response;
    }

    /**
     * @return the (optional) embedded certificate chain in the signed response.
     */
    public CertificateChain getCertificateChain() {

        return certificateChain;
    }
}
