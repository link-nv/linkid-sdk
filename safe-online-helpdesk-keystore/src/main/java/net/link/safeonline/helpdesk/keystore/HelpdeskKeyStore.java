/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


public class HelpdeskKeyStore extends AbstractFileBasedKeyStore {

    public HelpdeskKeyStore() {

        super("safe-online-helpdesk-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new HelpdeskKeyStore()._getPrivateKeyEntry();
    }
}
