/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.password;

import net.link.safeonline.Startable;


public interface PasswordConstants {

    public static final String PASSWORD_STARTABLE_JNDI_PREFIX      = "SafeOnlinePassword/startup/";

    public static final String PASSWORD_DEVICE_ID                  = "password";

    // Password device attributes
    public static final String PASSWORD_HASH_ATTRIBUTE             = "urn:net:lin-k:safe-online:attribute:password:hash";
    public static final String PASSWORD_SEED_ATTRIBUTE             = "urn:net:lin-k:safe-online:attribute:password:seed";
    public static final String PASSWORD_ALGORITHM_ATTRIBUTE        = "urn:net:lin-k:safe-online:attribute:password:algorithm";
    public static final String PASSWORD_NEW_ALGORITHM_ATTRIBUTE    = "urn:net:lin-k:safe-online:attribute:password:algorithm:new";
    public static final String PASSWORD_DEVICE_DISABLE_ATTRIBUTE   = "urn:net:lin-k:safe-online:attribute:password:device:disable";
    public static final String PASSWORD_DEVICE_ATTRIBUTE           = "urn:net:lin-k:safe-online:attribute:password:device";

    // Password WS authentication device credential attributes
    public static final String PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE    = "urn:net:lin-k:safe-online:password:ws:auth:login";
    public static final String PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE = "urn:net:lin-k:safe-online:password:ws:auth:password";

    public static final int    PASSWORD_BOOT_PRIORITY              = Startable.PRIORITY_BOOTSTRAP - 1;
}
