package net.link.safeonline.otpoversms.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.AbstractKeyStore;


public class OtpOverSmsKeyStore extends AbstractKeyStore {

    public OtpOverSmsKeyStore() {

        super("safe-online-otpoversms-keystore.jks");
    }

    public static PrivateKeyEntry getPrivateKeyEntry() {

        return new OtpOverSmsKeyStore()._getPrivateKeyEntry();
    }
}
