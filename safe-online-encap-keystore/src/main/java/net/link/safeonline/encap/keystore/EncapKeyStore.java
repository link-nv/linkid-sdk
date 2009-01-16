/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractKeyStore;


public class EncapKeyStore extends AbstractKeyStore {

    public EncapKeyStore() {

        super("safe-online-encap-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new EncapKeyStore()._getPrivateKeyEntry();
    }
}
