/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


public class OperKeyStore extends AbstractFileBasedKeyStore {

    public OperKeyStore() {

        super("safe-online-oper-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new OperKeyStore()._getPrivateKeyEntry();
    }
}
