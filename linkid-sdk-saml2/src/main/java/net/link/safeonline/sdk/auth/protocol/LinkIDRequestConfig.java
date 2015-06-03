/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import static net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder.config;

import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.util.logging.Logger;
import java.net.URI;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.configuration.LinkIDConfigUtils;


/**
 * Created by wvdhaute
 * Date: 29/01/14
 * Time: 16:04
 */
public class LinkIDRequestConfig {

    private static final Logger logger = Logger.get( LinkIDRequestConfig.class );

    private final String targetURL;
    private final String landingURL;
    private final String authnService;

    public static LinkIDRequestConfig get(final LinkIDAuthenticationContext authnContext) {

        String targetURL = authnContext.getTarget();
        if (targetURL == null || !URI.create( targetURL ).isAbsolute())
            targetURL = LinkIDConfigUtils.getApplicationURLForPath( targetURL );
        logger.dbg( "target url: %s", targetURL );

        String landingURL = null;
        if (config().web().landingPath() != null)
            landingURL = LinkIDConfigUtils.getApplicationConfidentialURLFromPath( config().web().landingPath() );
        logger.dbg( "landing url: %s", landingURL );

        if (landingURL == null) {
            // If no landing URL is configured, land on target.
            landingURL = targetURL;
            targetURL = null;
        }

        String authnService;
        if (authnContext.isMobileForceRegistration()) {
            authnService = String.format( "%s/%s", config().web().linkIDBase(), LinkIDConstants.LINKID_PATH_REG_MIN );
        } else {
            authnService = String.format( "%s/%s", config().web().linkIDBase(), LinkIDConstants.LINKID_PATH_AUTH_MIN );
        }

        return new LinkIDRequestConfig( targetURL, landingURL, authnService );
    }

    private LinkIDRequestConfig(final String targetURL, final String landingURL, final String authnService) {

        this.targetURL = targetURL;
        this.landingURL = landingURL;
        this.authnService = authnService;
    }

    // Accessors

    public String getTargetURL() {

        return targetURL;
    }

    public String getLandingURL() {

        return landingURL;
    }

    public String getAuthnService() {

        return authnService;
    }
}