/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


public class PasswordKeyStore extends AbstractFileBasedKeyStore {

    public PasswordKeyStore() {

        super("safe-online-password-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new PasswordKeyStore()._getPrivateKeyEntry();
    }
}
