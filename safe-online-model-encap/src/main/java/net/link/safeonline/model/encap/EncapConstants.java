/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap;

import net.link.safeonline.Startable;


public class EncapConstants {

    private EncapConstants() {

        // empty
    }


    public static final String ENCAP_STARTABLE_JNDI_PREFIX    = "SafeOnlineEncap/startup/";

    public static final String ENCAP_DEVICE_ID                = "encap";

    public static final String ENCAP_IDENTIFIER_DOMAIN        = "encap";

    public static final String ENCAP_MOBILE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:encap:mobile";

    public static final String ENCAP_DEVICE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:device:encap";

    public static final String ENCAP_DEVICE_DISABLE_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:device:encap:disable";

    public static final int    ENCAP_BOOT_PRIORITY            = Startable.PRIORITY_BOOTSTRAP - 1;
}
