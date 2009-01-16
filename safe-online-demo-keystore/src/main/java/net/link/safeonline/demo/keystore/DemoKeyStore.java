/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractKeyStore;


public class DemoKeyStore extends AbstractKeyStore {

    public static final String KEYSTORE_RESOURCE = "safe-online-demo-keystore.jks";


    public DemoKeyStore() {

        super(KEYSTORE_RESOURCE);
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new DemoKeyStore()._getPrivateKeyEntry();
    }
}
