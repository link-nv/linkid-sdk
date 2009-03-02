/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore.entity;

import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.KeyStoreUtils;


/**
 * <h2>{@link Type}<br>
 * <sub>The type of keystore to retrieve the key data from.</sub></h2>
 * 
 * <p>
 * This enumeration defines the abstract {@link #getPrivateKeyEntry(String)}, which retrieves the {@link PrivateKeyEntry} from the keystore
 * in a type-specific manner, given the type-specific configuration string.
 * </p>
 * 
 * <p>
 * Refer to the type-specific documentation for more information on how to use it.
 * </p>
 * 
 * <p>
 * <i>Jan 30, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public enum Type {

    /**
     * This type deals with the Java KeyStore format. These are file-based keystores.
     */
    JKS {

        /**
         * {@inheritDoc}
         * 
         * <p>
         * JKS-based:<br>
         * The key entry is loaded from a file-based keystore which is found at the location specified in the config string. The keystore's
         * password and the key entry's password are also specified in the config string. The keystore location is resolved by the active
         * thread's classloader.
         * </p>
         * 
         * @param config
         *            <code>[keyStorePassword]:[keyEntryPassword]:[keyStoreLocation]</code><br>
         *            <b>Note:</b> Neither the <code>keyStorePassword</code> or the <code>keyEntryPassword</code> can contain colons in the
         *            current implementation, since these are used as field delimiters. The <code>keyStoreLocation</code>, however, can.
         */
        @Override
        public PrivateKeyEntry getPrivateKeyEntry(String config) {

            String[] tokens = config.split(":", 3);
            String keyStorePassword = tokens[0];
            String keyEntryPassword = tokens[1];
            String keyStoreLocation = tokens[2];

            InputStream keyStoreStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreLocation);
            if (keyStoreStream == null)
                throw new IllegalStateException("Can't load keystore from config-specified location: " + keyStoreLocation);

            return KeyStoreUtils.loadPrivateKeyEntry("jks", keyStoreStream, keyStorePassword, keyEntryPassword);
        }
    };

    /**
     * @return The {@link PrivateKeyEntry} as resolved by this type using the given config string in the type-specific manner.
     */
    public abstract PrivateKeyEntry getPrivateKeyEntry(String config);
}
