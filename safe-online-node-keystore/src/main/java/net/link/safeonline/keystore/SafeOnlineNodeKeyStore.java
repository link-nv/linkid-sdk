/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.keystore;

import java.security.KeyStore.PrivateKeyEntry;


public class SafeOnlineNodeKeyStore extends AbstractServiceBasedKeyStore {

    public SafeOnlineNodeKeyStore() {

        super(/* Something, don't know what yet. */);
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new SafeOnlineNodeKeyStore()._getPrivateKeyEntry();
    }
}
