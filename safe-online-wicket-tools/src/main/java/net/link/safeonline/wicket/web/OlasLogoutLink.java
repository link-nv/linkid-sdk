/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.apache.wicket.Page;
import org.apache.wicket.Session;


/**
 * <h2>{@link OlasLoginLink}<br>
 * <sub>A link that uses the OLAS SDK to log a user out of this application and all other applications in its SSO pool through the OLAS
 * authentication services.</sub></h2>
 * 
 * <p>
 * <i>Sep 22, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class OlasLogoutLink extends OlasAuthLink {

    private static final long serialVersionUID = 1L;


    public OlasLogoutLink(String id) {

        super(id);
    }

    public OlasLogoutLink(String id, Class<? extends Page> target) {

        super(id, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void delegate(String target, HttpServletRequest request, HttpServletResponse response) {

        if (LoginManager.isAuthenticated(request)) {
            LOG.debug("Logout delegated to OLAS with target: " + target);
            SafeOnlineLoginUtils.logout(target, request, response);
        }

        else {
            LOG.debug("Logout handeled locally; invalidating session.");
            // Technically, the Wicket session, being on the HTTP session doesn't need to be invalidated manually.
            // However, inside Unit tests, just invalidating the MockHttpSession doesn't seem to be enough.
            if (Session.exists()) {
                Session.get().invalidate();
            }
            request.getSession().invalidate();

            try {
                response.sendRedirect(target);
            } catch (IOException e) {
                LOG.error("couldn't redirect to target after logout.", e);
            }
        }
    }
}
