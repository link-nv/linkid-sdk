/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.HashMap;
import java.util.Map;


/**
 * <h2>{@link AbstractKeyStore}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 15, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractKeyStore {

    private static Map<ClassLoader, Map<String, PrivateKeyEntry>> privateKeyEntries;
    private String                                                keyStoreResource;


    public AbstractKeyStore(String keyStoreResource) {

        this.keyStoreResource = keyStoreResource;
    }

    public PrivateKeyEntry _getPrivateKeyEntry() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (privateKeyEntries == null) {
            privateKeyEntries = new HashMap<ClassLoader, Map<String, PrivateKeyEntry>>();
        }
        if (!privateKeyEntries.containsKey(classLoader)) {
            privateKeyEntries.put(classLoader, new HashMap<String, PrivateKeyEntry>());
        }
        if (!privateKeyEntries.containsKey(keyStoreResource)) {
            InputStream keyStoreInputStream = classLoader.getResourceAsStream(keyStoreResource);
            if (null == keyStoreInputStream)
                throw new RuntimeException("keystore not found: " + keyStoreResource);

            PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry("jks", keyStoreInputStream, "secret", "secret");
            privateKeyEntries.get(classLoader).put(keyStoreResource, privateKeyEntry);
        }

        return privateKeyEntries.get(classLoader).get(keyStoreResource);
    }
}
