/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


public class DemoMandateKeyStore extends AbstractFileBasedKeyStore {

    public static final String KEYSTORE_RESOURCE = "safe-online-demo-mandate-keystore.jks";


    public DemoMandateKeyStore() {

        super(KEYSTORE_RESOURCE);
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new DemoMandateKeyStore()._getPrivateKeyEntry();
    }
}
