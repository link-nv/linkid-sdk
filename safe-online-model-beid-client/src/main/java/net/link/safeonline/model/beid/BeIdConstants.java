/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid;

import net.link.safeonline.Startable;


public interface BeIdConstants {

    public static final String BEID_STARTABLE_JNDI_PREFIX        = "SafeOnlineBeid/startup/";

    public static final String BEID_DEVICE_ID                    = "beid";

    // BeId device attributes
    public static final String BEID_SURNAME_ATTRIBUTE            = "urn:net:lin-k:safe-online:attribute:beid:surname";
    public static final String BEID_GIVENNAME_ATTRIBUTE          = "urn:net:lin-k:safe-online:attribute:beid:givenName";
    public static final String BEID_AUTH_CERT_ATTRIBUTE          = "urn:net:lin-k:safe-online:attribute:beid:authcert";
    public static final String BEID_NRN_ATTRIBUTE                = "urn:net:lin-k:safe-online:attribute:beid:nrn";
    public static final String BEID_IDENTIFIER_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:beid:identifier";
    public static final String BEID_DEVICE_ATTRIBUTE             = "urn:net:lin-k:safe-online:attribute:beid:device";
    public static final String BEID_DEVICE_USER_ATTRIBUTE        = "urn:net:lin-k:safe-online:attribute:beid:device:user";
    public static final String BEID_DEVICE_DISABLE_ATTRIBUTE     = "urn:net:lin-k:safe-online:attribute:beid:device:disable";

    // BeId WS authentication device credential attributes
    public static final String BEID_WS_AUTH_STATEMENT_ATTRIBUTE  = "urn:net:lin-k:safe-online:beid:ws:auth:statement";
    // BeId WS authentication device information attributes
    public static final String BEID_WS_AUTH_SESSION_ID_ATTRIBUTE = "urn:net:lin-k:safe-online:beid:ws:auth:sessionId";

    public static final int    BEID_BOOT_PRIORITY                = Startable.PRIORITY_BOOTSTRAP - 1;

}
