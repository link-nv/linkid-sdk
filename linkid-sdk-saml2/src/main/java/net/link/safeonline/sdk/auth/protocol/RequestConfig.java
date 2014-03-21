/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import net.link.util.logging.Logger;
import java.net.URI;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.ConfigUtils;


/**
 * Created by wvdhaute
 * Date: 29/01/14
 * Time: 16:04
 */
public class RequestConfig {

    private static final Logger logger = Logger.get( RequestConfig.class );

    private final String targetURL;
    private final String landingURL;
    private final String authnService;

    public static RequestConfig get(final AuthenticationContext authnContext) {

        String targetURL = authnContext.getTarget();
        if (targetURL == null || !URI.create( targetURL ).isAbsolute())
            targetURL = ConfigUtils.getApplicationURLForPath( targetURL );
        logger.dbg( "target url: %s", targetURL );

        String landingURL = null;
        if (config().web().landingPath() != null)
            landingURL = ConfigUtils.getApplicationConfidentialURLFromPath( config().web().landingPath() );
        logger.dbg( "landing url: %s", landingURL );

        if (landingURL == null) {
            // If no landing URL is configured, land on target.
            landingURL = targetURL;
            targetURL = null;
        }

        String authnService;
        if (authnContext.isMobileForceRegistration())
            authnService = config().web().mobileRegMinimalURL();
        else
            authnService = config().web().mobileAuthMinimalURL();

        return new RequestConfig( targetURL, landingURL, authnService );
    }

    private RequestConfig(final String targetURL, final String landingURL, final String authnService) {

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
