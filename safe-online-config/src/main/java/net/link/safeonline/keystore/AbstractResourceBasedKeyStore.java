/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.config;

import com.google.common.io.Closeables;
import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import net.link.util.common.KeyStoreUtils;


/**
 * <h2>{@link AbstractResourceBasedKeyStore}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class AbstractResourceBasedKeyStore extends AbstractKeyStore {

    private static final Map<ClassLoader, Map<String, PrivateKeyEntry>> privateKeyEntries = new HashMap<ClassLoader, Map<String, PrivateKeyEntry>>();
    private final String keyStoreResource;

    public AbstractResourceBasedKeyStore(String keyStoreResource) {

        this.keyStoreResource = keyStoreResource;
    }

    /**
     * {@inheritDoc}
     */
    public PrivateKeyEntry _getPrivateKeyEntry() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!privateKeyEntries.containsKey( classLoader ))
            privateKeyEntries.put( classLoader, new HashMap<String, PrivateKeyEntry>() );

        if (!privateKeyEntries.get( classLoader ).containsKey( keyStoreResource )) {
            InputStream keyStoreInputStream = classLoader.getResourceAsStream( keyStoreResource );
            try {
                if (null == keyStoreInputStream)
                    throw new RuntimeException( "keystore not found: " + keyStoreResource );

                PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry( "jks", keyStoreInputStream,
                                                                                     config().linkID().app().keyStorePass(),
                                                                                     config().linkID().app().keyEntryPass(),
                                                                                     config().linkID().app().keyEntryAlias() );
                privateKeyEntries.get( classLoader ).put( keyStoreResource, privateKeyEntry );
            } finally {
                Closeables.closeQuietly( keyStoreInputStream );
            }
        }

        return privateKeyEntries.get( classLoader ).get( keyStoreResource );
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, X509Certificate> getOtherCertificates() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!privateKeyEntries.containsKey( classLoader ))
            privateKeyEntries.put( classLoader, new HashMap<String, PrivateKeyEntry>() );

        InputStream keyStoreInputStream = classLoader.getResourceAsStream( keyStoreResource );
        try {
            if (null == keyStoreInputStream)
                throw new RuntimeException( "keystore not found: " + keyStoreResource );

            return KeyStoreUtils.loadOtherCertificates( "jks", keyStoreInputStream, config().linkID().app().keyStorePass(), null );
        } finally {
            Closeables.closeQuietly( keyStoreInputStream );
        }
    }
}
