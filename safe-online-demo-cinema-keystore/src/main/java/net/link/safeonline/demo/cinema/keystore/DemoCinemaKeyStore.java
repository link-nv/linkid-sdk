/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


public class DemoCinemaKeyStore extends AbstractFileBasedKeyStore {

    public static final String KEYSTORE_RESOURCE = "safe-online-demo-cinema-keystore.jks";


    public DemoCinemaKeyStore() {

        super(KEYSTORE_RESOURCE);
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new DemoCinemaKeyStore()._getPrivateKeyEntry();
    }
}
