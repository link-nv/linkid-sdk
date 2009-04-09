/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineAuthenticationUtils;

import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
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
public class OlasLogoutLink extends AbstractOlasAuthLink {

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
    public void delegate(Class<? extends Page> target, HttpServletRequest request, HttpServletResponse response) {

        boolean redirected = false;
        if (LoginManager.isAuthenticated(request)) {
            String targetUrl = RequestCycle.get().urlFor(target, null).toString();
            LOG.debug("Logout delegated to OLAS with target: " + targetUrl);
            redirected = SafeOnlineAuthenticationUtils.logout(targetUrl, session, request, response);
        }

        if (!redirected) {
            LOG.debug("Logout handeled locally; invalidating session.");
            Session.get().invalidateNow();

            throw new RestartResponseException(target);
        }
    }
}
