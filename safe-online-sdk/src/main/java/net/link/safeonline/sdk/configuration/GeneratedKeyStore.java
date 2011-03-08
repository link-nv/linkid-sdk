package net.link.safeonline.sdk.configuration;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import net.link.safeonline.keystore.AbstractKeyStore;

/**
 * <h2>{@link net.link.safeonline.sdk.configuration.GeneratedKeyStore}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>10 05, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class GeneratedKeyStore extends AbstractKeyStore {

    private transient KeyStore.PrivateKeyEntry keyEntry;

    public KeyStore.PrivateKeyEntry _getPrivateKeyEntry() {

        if (keyEntry == null)
            try {
                keyEntry = load();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException( e );
            } catch (IOException e) {
                throw new RuntimeException( e );
            }

        return keyEntry;
    }

    public Map<String, X509Certificate> getOtherCertificates() {

        return Collections.emptyMap();
    }

    protected abstract KeyStore.PrivateKeyEntry load()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException, IOException, InvalidKeyException,
                   CertificateException;
}
