package net.link.safeonline.otpoversms.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractFileBasedKeyStore;


public class OtpOverSmsKeyStore extends AbstractFileBasedKeyStore {

    public OtpOverSmsKeyStore() {

        super("safe-online-otpoversms-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new OtpOverSmsKeyStore()._getPrivateKeyEntry();
    }
}
