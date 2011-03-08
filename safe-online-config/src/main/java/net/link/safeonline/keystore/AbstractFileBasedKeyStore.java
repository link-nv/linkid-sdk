/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.config;

import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import net.link.util.common.KeyStoreUtils;


/**
 * <h2>{@link AbstractFileBasedKeyStore}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class AbstractFileBasedKeyStore extends AbstractKeyStore {

    private static final Map<File, PrivateKeyEntry> privateKeyEntries = new HashMap<File, PrivateKeyEntry>();
    private final File keyStoreFile;

    public AbstractFileBasedKeyStore(File keyStoreFile) {

        this.keyStoreFile = keyStoreFile;
    }

    /**
     * {@inheritDoc}
     */
    public PrivateKeyEntry _getPrivateKeyEntry() {

        if (!privateKeyEntries.containsKey( keyStoreFile )) {
            InputStream keyStoreInputStream = null;
            try {
                keyStoreInputStream = new FileInputStream( keyStoreFile );
                PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry( "jks", keyStoreInputStream,
                                                                                     config().linkID().app().keyStorePass(),
                                                                                     config().linkID().app().keyEntryPass(),
                                                                                     config().linkID().app().keyEntryAlias() );
                privateKeyEntries.put( keyStoreFile, privateKeyEntry );
            } catch (IOException e) {
                throw new RuntimeException( "couldn't load keystore file: " + keyStoreFile, e );
            } finally {
                Closeables.closeQuietly( keyStoreInputStream );
            }
        }

        return privateKeyEntries.get( keyStoreFile );
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, X509Certificate> getOtherCertificates() {

        InputStream keyStoreInputStream = null;
        try {
            keyStoreInputStream = new FileInputStream( keyStoreFile );
            return KeyStoreUtils.loadOtherCertificates( "jks", keyStoreInputStream, config().linkID().app().keyStorePass(), null );
        } catch (IOException e) {
            throw new RuntimeException( "couldn't load keystore file: " + keyStoreFile, e );
        } finally {
            Closeables.closeQuietly( keyStoreInputStream );
        }
    }
}
