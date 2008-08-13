/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.demo.keystore.DemoKeyStoreUtils;


public class DemoPrescriptionKeyStoreUtils {

    public static final String KEYSTORE_RESOURCE = "safe-online-demo-prescription-keystore.jks";


    public static PrivateKeyEntry getPrivateKeyEntry() {

        return DemoKeyStoreUtils.getPrivateKeyEntry(KEYSTORE_RESOURCE);
    }
}
