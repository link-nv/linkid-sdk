/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractKeyStore;


public class DigipassKeyStore extends AbstractKeyStore {

    public DigipassKeyStore() {

        super("safe-online-digipass-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new DigipassKeyStore()._getPrivateKeyEntry();
    }
}
