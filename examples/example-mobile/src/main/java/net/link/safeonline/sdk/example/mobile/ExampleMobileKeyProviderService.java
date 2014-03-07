/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile;

import net.link.util.config.ResourceKeyStoreKeyProvider;


public class ExampleMobileKeyProviderService extends ResourceKeyStoreKeyProvider {

    public ExampleMobileKeyProviderService() {

        super( "example-mobile-keystore.jks", "secret", "example-mobile", "secret" );
    }
}
