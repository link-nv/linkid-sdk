/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.resources;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.web.Session;


/**
 * <h2>{@link LogoutServlet}<br>
 * <sub>Logout servlet.</sub></h2>
 * 
 * <p>
 * Servlet that performs a logout for the SafeOnline web applications.
 * </p>
 * 
 * <p>
 * <i>Sep 19, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class LogoutServlet extends AbstractInjectionServlet {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(LogoutServlet.class);

    @Init(name = "LogoutExitUrl")
    private String            logoutExitUrl;


    @Override
    public void invokeGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOG.debug("invoke get");

        LoginManager.removeUserId(request);
        removeLoginCookie(request, response);
        if (Contexts.isSessionContextActive()) {
            Session.getInstance().invalidate();
        }

        response.sendRedirect(this.logoutExitUrl);

    }

    private void removeLoginCookie(HttpServletRequest request, HttpServletResponse response) {

        LOG.debug("remove login cookie");

        Cookie loginCookie = new Cookie(SafeOnlineCookies.LOGIN_COOKIE, "");
        loginCookie.setPath(request.getContextPath());
        loginCookie.setMaxAge(0);
        response.addCookie(loginCookie);
    }

}
