/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractKeyStore;


public class DemoPaymentKeyStore extends AbstractKeyStore {

    public static final String KEYSTORE_RESOURCE = "safe-online-demo-payment-keystore.jks";


    public DemoPaymentKeyStore() {

        super(KEYSTORE_RESOURCE);
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new DemoPaymentKeyStore()._getPrivateKeyEntry();
    }
}
