/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.pki;

import java.security.cert.X509Certificate;


/**
 * Interface for linkID PKI client.
 *
 * @author fcorneli
 */
public interface PkiClient {

    /**
     * Gives back the linkID certificate that is used to sign the SAML authentication tokens.
     */
    X509Certificate getCertificate();
}
