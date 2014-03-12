/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.util;

import com.lyndir.lhunath.opal.system.logging.Logger;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.LogoutContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Utility class for Authentication against LinkID.
 *
 * @author fcorneli
 */
public abstract class AuthenticationUtils {

    private static final Logger logger = Logger.get( AuthenticationUtils.class );

    /**
     * Initiates linkID authentication by redirecting the user to the linkID authentication application.
     */
    public static void login(HttpServletRequest request, HttpServletResponse response) {

        login( request, response, new AuthenticationContext() );
    }

    /**
     * Initiates linkID authentication by redirecting the user to the linkID authentication application.
     *
     * @param context The authentication context can be used to configure the parameters of this authentication request in detail.
     */
    public static void login(HttpServletRequest request, HttpServletResponse response, @NotNull AuthenticationContext context) {

        try {
            ProtocolManager.initiateAuthentication( request, response, context );
            logger.dbg( "executed protocol" );
        }
        catch (IOException e) {
            throw new RuntimeException( "could not initiate authentication", e );
        }
    }

    /**
     * @see #logout(HttpServletRequest, HttpServletResponse, String, LogoutContext)
     */
    public static boolean logout(HttpServletRequest request, HttpServletResponse response) {

        return logout( request, response, new LogoutContext() );
    }

    /**
     * @see #logout(HttpServletRequest, HttpServletResponse, String, LogoutContext)
     */
    public static boolean logout(HttpServletRequest request, HttpServletResponse response, @NotNull LogoutContext context) {

        return logout( request, response, null, context );
    }

    /**
     * <p>
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * </p>
     * <p/>
     * Performs a SafeOnline logout using the SafeOnline authentication web application.
     *
     * @param request  The servlet request is used to access servlet configuration information.
     * @param response The servlet response on which to send out the logout request.
     * @param userId   The user ID of the subject logging out.  If null, the SDK will look for the userId in the SDK credentials on the session.
     *
     * @return <code>true</code> if redirected successful, <code>false</code> if not redirected ( e.g. SSO is disabled or no user is logged in )
     */
    public static boolean logout(HttpServletRequest request, HttpServletResponse response, @Nullable String userId, @NotNull LogoutContext context) {

        if (userId == null) {
            userId = LoginManager.findUserId( request.getSession() );

            if (userId == null)
                // Nobody logged in.
                return false;
        }

        /* Initialize and execute the authentication protocol. */
        try {
            ProtocolManager.initiateLogout( request, response, userId, context );
            logger.dbg( "executed protocol" );
        }
        catch (IOException e) {
            throw new RuntimeException( "could not initiate logout", e );
        }

        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String getDeviceIdentifier(String deviceAuthnContextClass, String deviceId) {

        return deviceAuthnContextClass + ":" + deviceId;
    }
}
