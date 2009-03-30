/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.option;

import net.link.safeonline.Startable;


public interface OptionConstants {

    public static final String OPTION_STARTABLE_JNDI_PREFIX    = "SafeOnlineOption/startup/";

    public static final String OPTION_IDENTIFIER_DOMAIN        = "option";

    // Option device attributes
    public static final String OPTION_DEVICE_ID                = "option";
    public static final String OPTION_DEVICE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:option:device";
    public static final String OPTION_IMEI_ATTRIBUTE           = "urn:net:lin-k:safe-online:attribute:option:device:imei";
    public static final String OPTION_DEVICE_DISABLE_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:option:device:disable";

    // Option WS authentication device credential attributes
    public static final String OPTION_WS_AUTH_IMEI_ATTRIBUTE   = "urn:net:lin-k:safe-online:option:ws:auth:imei";

    public static final int    OPTION_BOOT_PRIORITY            = Startable.PRIORITY_BOOTSTRAP - 1;
}
