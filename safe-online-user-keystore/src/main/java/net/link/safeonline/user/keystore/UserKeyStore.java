/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


public class UserKeyStore extends AbstractFileBasedKeyStore {

    public UserKeyStore() {

        super("safe-online-user-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new UserKeyStore()._getPrivateKeyEntry();
    }
}
