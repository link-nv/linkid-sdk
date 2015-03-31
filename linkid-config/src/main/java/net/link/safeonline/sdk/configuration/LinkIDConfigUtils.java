/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import static net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder.config;

import net.link.util.common.URLUtils;


/**
 * <h2>{@link LinkIDConfigUtils}<br>
 * <sub>Utility class for {@link LinkIDSDKConfigHolder}.</sub></h2>
 * <p/>
 * <p>
 * This utility class provides convenience methods that uses properties defined in {@link LinkIDSDKConfigHolder}.
 * </p>
 * <p/>
 * <p>
 * <i>Sep 8, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class LinkIDConfigUtils {

    public static final String SSL_ALIAS = "ws-ssl";

    /**
     * <b>URLs returned by this method are not confidentiality-safe.</b>
     *
     * @return The absolute base URL for the application that was activated by the given servlet request (URL to its context path).
     */
    public static String getApplicationURL() {

        return URLUtils.concat( config().web().appBase(), config().web().appPath() );
    }

    /**
     * <b>URLs returned by this method are not confidentiality-safe.</b>
     *
     * @param path The path relative to the application's context path to generate a URL for.
     *
     * @return The absolute URL to the given path within the application that was activated by the given servlet request.
     */
    public static String getApplicationURLForPath(String path) {

        String applicationURL = getApplicationURL();
        if (path == null || applicationURL.endsWith( path ))
            return applicationURL;

        return URLUtils.concat( applicationURL, path );
    }

    /**
     * <b>You can rely on the fact that URLs returned by this method are configured by the user to protect the confidentiality between
     * server and client (eg. they're on HTTPS).</b>
     *
     * @return The absolute confidential base URL for the application that was activated by the given servlet request (URL to its context
     * path).
     */
    public static String getApplicationConfidentialURL() {

        return URLUtils.concat( config().web().appConfidentialBase(), config().web().appPath() );
    }

    /**
     * <b>You can rely on the fact that URLs returned by this method are configured by the user to protect the confidentiality between
     * server and client (eg. they're on HTTPS).</b>
     *
     * @param path The path relative to the application's context path to generate a URL for.
     *
     * @return The absolute URL to the given path within the application that was activated by the given servlet request.
     */
    public static String getApplicationConfidentialURLFromPath(String path) {

        return URLUtils.concat( getApplicationConfidentialURL(), path );
    }
}
