/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.LogoutProtocolRequestContext;
import net.link.safeonline.sdk.auth.protocol.LogoutProtocolResponseContext;


public class LogoutServlet extends AbstractLogoutServlet {


    @Override
    protected boolean logout(HttpSession session, LogoutProtocolRequestContext context) {

        if (context != null && !context.getUserId().equals( LoginManager.findUserId( session ) ))
            // Logout request was not for the currently logged-in user.
            return true;

        session.invalidate();

        return true;
    }

    @Override
    protected boolean logout(HttpSession session, LogoutProtocolResponseContext context) {

        if (context != null && !context.getRequest().getUserId().equals( LoginManager.findUserId( session ) ))
            // Logout request was not for the currently logged-in user.
            return false;

        session.invalidate();
        return true;
    }
}
