/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc.spi;

import java.security.cert.X509Certificate;


public interface IdentityDataExtractor {

    void init(IdentityDataCollector identityDataCollector);

    void prePkcs11();

    void postPkcs11(X509Certificate authenticationCertificate);
}
