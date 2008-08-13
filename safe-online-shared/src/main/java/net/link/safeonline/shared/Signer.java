/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared;

import java.security.cert.X509Certificate;


/**
 * Interface for a signer component. The implementation can be PKCS#11 or PC/SC based, depending on the runtime
 * environment.
 *
 * @author fcorneli
 *
 */
public interface Signer {

    /**
     * Signs the given byte sequence. Signature algorithm: SHA1-RSA
     *
     * @param data
     * @return
     */
    byte[] sign(byte[] data);

    /**
     * Gives back the corresponding X509 certificate that is used for signing.
     *
     * @return
     */
    X509Certificate getCertificate();
}
