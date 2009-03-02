/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk;

import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;


/**
 * <h2>{@link KeyStoreFactory}<br>
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
public abstract class KeyStoreFactory {

    private String keyStoreResource;


    public KeyStoreFactory(String keyStoreResource) {

        this.keyStoreResource = keyStoreResource;
    }

    public PrivateKeyEntry getPrivateKeyEntry() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream keyStoreInputStream = classLoader.getResourceAsStream(keyStoreResource);
        if (null == keyStoreInputStream)
            throw new RuntimeException("keystore not found: " + keyStoreResource);

        return KeyStoreUtils.loadPrivateKeyEntry("jks", keyStoreInputStream, "secret", "secret");
    }
}
