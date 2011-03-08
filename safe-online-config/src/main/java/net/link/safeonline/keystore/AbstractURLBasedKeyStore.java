/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.config;

import com.google.common.io.Closeables;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import net.link.util.common.KeyStoreUtils;


/**
 * <h2>{@link AbstractURLBasedKeyStore}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class AbstractURLBasedKeyStore extends AbstractKeyStore {

    private static final Map<URL, PrivateKeyEntry> privateKeyEntries = new HashMap<URL, PrivateKeyEntry>();
    private final URL keyStoreURL;

    protected AbstractURLBasedKeyStore(final URL keyStoreURL) {

        this.keyStoreURL = keyStoreURL;
    }

    /**
     * {@inheritDoc}
     */
    public PrivateKeyEntry _getPrivateKeyEntry() {

        if (!privateKeyEntries.containsKey( keyStoreURL )) {
            InputStream keyStoreInputStream = null;
            try {
                keyStoreInputStream = keyStoreURL.openStream();
                PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry( "jks", keyStoreInputStream,
                                                                                     config().linkID().app().keyStorePass(),
                                                                                     config().linkID().app().keyEntryPass(),
                                                                                     config().linkID().app().keyEntryAlias() );
                privateKeyEntries.put( keyStoreURL, privateKeyEntry );
            } catch (IOException e) {
                throw new RuntimeException( "couldn't load keystore url: " + keyStoreURL, e );
            } finally {
                Closeables.closeQuietly( keyStoreInputStream );
            }
        }

        return privateKeyEntries.get( keyStoreURL );
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, X509Certificate> getOtherCertificates() {

        InputStream keyStoreInputStream = null;
        try {
            keyStoreInputStream = keyStoreURL.openStream();
            return KeyStoreUtils.loadOtherCertificates( "jks", keyStoreInputStream, config().linkID().app().keyStorePass(), null );
        } catch (IOException e) {
            throw new RuntimeException( "couldn't load keystore url: " + keyStoreURL, e );
        } finally {
            Closeables.closeQuietly( keyStoreInputStream );
        }
    }
}
