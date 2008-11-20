/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid;

import net.link.safeonline.Startable;


public interface BeIdConstants {

    public static final String BEID_STARTABLE_JNDI_PREFIX    = "SafeOnlineBeid/startup/";

    public static final String BEID_DEVICE_ID                = "beid";

    public static final String SURNAME_ATTRIBUTE             = "urn:net:lin-k:safe-online:attribute:beid:surname";

    public static final String GIVENNAME_ATTRIBUTE           = "urn:net:lin-k:safe-online:attribute:beid:givenName";

    public static final String AUTH_CERT_ATTRIBUTE           = "urn:net:lin-k:safe-online:attribute:beid:authcert";

    public static final String NRN_ATTRIBUTE                 = "urn:net:lin-k:safe-online:attribute:beid:nrn";

    public static final String BEID_DEVICE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:device:beid";

    public static final String BEID_DEVICE_USER_ATTRIBUTE    = "urn:net:lin-k:safe-online:attribute:device:beid:user";

    public static final String BEID_DEVICE_DISABLE_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:device:beid:disable";

    public static final int    BEID_BOOT_PRIORITY            = Startable.PRIORITY_BOOTSTRAP - 1;

}
