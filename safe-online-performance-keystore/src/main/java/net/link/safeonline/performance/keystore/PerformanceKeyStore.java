/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


/**
 * <h2>{@link PerformanceKeyStore}<br>
 * <sub>Access to performance-application keys from the keystore.</sub></h2>
 * 
 * <p>
 * The private key and certificate will be lazily loaded from the default keystore file if not yet available upon the first request for it.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class PerformanceKeyStore extends AbstractFileBasedKeyStore {

    public PerformanceKeyStore() {

        super("safe-online-performance-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new PerformanceKeyStore()._getPrivateKeyEntry();
    }
}
