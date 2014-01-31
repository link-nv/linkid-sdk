package net.link.safeonline.sdk.example.mobile.x509;

import net.link.util.config.ResourceKeyStoreKeyProvider;


public class ExampleMobileKeyProviderService extends ResourceKeyStoreKeyProvider {

    public ExampleMobileKeyProviderService() {

        super( "example-mobile-keystore.jks", "secret", "example-mobile", "secret" );
    }
}
