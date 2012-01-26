/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.config;

import java.util.HashMap;
import java.util.Map;


/**
 * Error codes used by the configuration WS.
 *
 * @author wvdhaute
 */
public enum ConfigurationStatusCode {

    APPLICATION_NOT_FOUND( "urn:net:lin-k:safe-online:ws:config:status:ApplicationNotFound" ),
    INTERNAL_ERROR( "urn:net:lin-k:safe-online:ws:config:status:InternalError" ),
    SUCCESS( "urn:net:lin-k:safe-online:ws:config:status:Success" );

    private final String urn;

    private static final Map<String, ConfigurationStatusCode> urnMap = new HashMap<String, ConfigurationStatusCode>();

    static {
        ConfigurationStatusCode[] statusCodes = ConfigurationStatusCode.values();
        for (ConfigurationStatusCode statusCode : statusCodes)
            urnMap.put( statusCode.getURN(), statusCode );
    }

    ConfigurationStatusCode(String urn) {

        this.urn = urn;
    }

    public String getURN() {

        return urn;
    }

    @Override
    public String toString() {

        return urn;
    }

    public static ConfigurationStatusCode ofURN(String urn) {

        ConfigurationStatusCode configurationStatusCode = urnMap.get( urn );
        if (null == configurationStatusCode)
            throw new IllegalArgumentException( "unknown ws configuration error code: " + urn );
        return configurationStatusCode;
    }
}
