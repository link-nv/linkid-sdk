/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option;

import net.link.safeonline.Startable;


/**
 * <h2>{@link OptionConstants}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
public interface OptionConstants {

    public static final String OPTION_STARTABLE_JNDI_PREFIX    = "SafeOnlineOption/startup/";

    public static final String OPTION_DEVICE_ID                = "option";

    public static final String OPTION_IDENTIFIER_DOMAIN        = "option";

    public static final String IMEI_OPTION_ATTRIBUTE           = "urn:net:lin-k:safe-online:attribute:option:imei";

    public static final String PIN_OPTION_ATTRIBUTE            = "urn:net:lin-k:safe-online:attribute:option:pin";

    public static final String OPTION_DEVICE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:device:option";

    public static final String OPTION_DEVICE_DISABLE_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:device:option:disable";

    public static final int    OPTION_BOOT_PRIORITY            = Startable.PRIORITY_BOOTSTRAP - 1;

}
