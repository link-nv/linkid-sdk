/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.keystore;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.KeyStore.PrivateKeyEntry;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class SafeOnlineNodeKeyStore extends AbstractServiceBasedKeyStore {

    private static SecretKey ssoKey;


    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new SafeOnlineNodeKeyStore()._getPrivateKeyEntry();
    }

    public static SecretKey getSSOKey() {

        if (ssoKey == null) {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128, new SecureRandom());
                ssoKey = keyGen.generateKey();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        return ssoKey;
    }
}
