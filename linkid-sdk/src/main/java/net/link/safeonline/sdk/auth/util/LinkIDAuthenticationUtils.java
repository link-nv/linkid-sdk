/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.util;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.protocol.LinkIDProtocolManager;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.util.logging.Logger;
import org.jetbrains.annotations.NotNull;


/**
 * Utility class for Authentication against LinkID.
 *
 * @author fcorneli
 */
public abstract class LinkIDAuthenticationUtils {

    private static final Logger logger = Logger.get( LinkIDAuthenticationUtils.class );

    /**
     * Initiates linkID authentication by redirecting the user to the linkID authentication application.
     */
    public static void login(HttpServletRequest request, HttpServletResponse response) {

        login( request, response, new LinkIDAuthenticationContext() );
    }

    /**
     * Initiates linkID authentication by redirecting the user to the linkID authentication application.
     *
     * @param context The authentication context can be used to configure the parameters of this authentication request in detail.
     */
    public static void login(HttpServletRequest request, HttpServletResponse response, @NotNull LinkIDAuthenticationContext context) {

        try {
            LinkIDProtocolManager.initiateAuthentication( request, response, context );
            logger.dbg( "executed protocol" );
        }
        catch (IOException e) {
            throw new RuntimeException( "could not initiate authentication", e );
        }
    }
}
