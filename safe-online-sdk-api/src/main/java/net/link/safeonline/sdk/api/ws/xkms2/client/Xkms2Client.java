/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.xkms2.client;

import java.security.cert.CertificateEncodingException;
import net.link.safeonline.sdk.api.exception.ValidationFailedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.util.common.CertificateChain;


/**
 * Interface for XKMS 2.0 client.
 *
 * @author wvdhaute
 */
public interface Xkms2Client {

    /**
     * Validate the linkID certificate chain.
     *
     * @param certificateChain the linkID certificate chain.
     *
     * @throws WSClientTransportException   something went wrong sending the XKMS 2.0 Validation Request.
     * @throws ValidationFailedException    validation failed.
     * @throws CertificateEncodingException failed to encode a certificate in the chain.
     */
    void validate(CertificateChain certificateChain)
            throws WSClientTransportException, ValidationFailedException, CertificateEncodingException;
}
