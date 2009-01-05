/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import javax.servlet.ServletException;

import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;


/**
 * <h2>{@link OlasApplicationPage}<br>
 * <sub>An abstract {@link WebPage} that provides some OLAS-specific features to web applications.</sub></h2>
 * 
 * <p>
 * This page takes care of synchronizing the application-level user with the OLAS user. This synchronization involves asking the application
 * to create an application-level user after a successful OLAS login has been completed, and invalidating the wicket {@link Session} when
 * the OLAS user has changed or been logged out.
 * </p>
 * 
 * <p>
 * We also provide a post-authentication mechanism which is used when users go to a page that requires authentication without being
 * authenticated yet. The user is sent to an authentication page (as defined by {@link Authenticated#redirect()}), and after a successful
 * authentication the user is sent back to the page he first tried to view. (See {@link #postAuth()})
 * </p>
 * 
 * <p>
 * <i>Dec 15, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class OlasApplicationPage extends WicketPage {

    public OlasApplicationPage() {

        // Check whether OLAS user has changed or been logged out.
        try {
            if (OLASSession.get().getUserOlasId() != null && OLASSession.get().getUserOlasId() != WicketUtil.getOlasId(getRequest()))
                throw new ServletException("User changed.");
        }

        catch (ServletException e) {
            // If wicket session has an OLAS id and no OLAS user is logged in, or a different one is logged in:
            // Invalidate session & retry.
            OLASSession.get().invalidateNow();
            throw new RestartResponseException(getClass());
        }

        // If just logged in using OLAS, let application create/push its user onto wicket session.
        if (!OLASSession.get().isUserSet() && WicketUtil.isOlasAuthenticated(getRequest())) {
            onOlasAuthenticated();
            postAuth();
        }

        // Enforce page requirements for user authentication.
        if (getClass().isAnnotationPresent(Authenticated.class) && !OLASSession.get().isUserSet()) {
            Authenticated authAnnotation = getClass().getAnnotation(Authenticated.class);
            Class<? extends Page> redirect = authAnnotation.redirect();

            if (!redirect.equals(Page.class)) {
                OLASSession.get().setPostAuthenticationPage(this);
                throw new RestartResponseException(redirect);
            }

            throw new AbortWithWebErrorCodeException(HttpStatus.SC_UNAUTHORIZED);
        }
    }

    /**
     * If application created user successfully, perform post-authentication.
     * 
     * <p>
     * Post Authentication will redirect the user back to the page he was on when he was forced to authenticate before being allowed to see
     * the page. If there is no post authentication page set, nothing is done here.
     * </p>
     * 
     * <p>
     * Called by {@link OlasApplicationPage} after successfully logging in with OLAS.<br>
     * You should call this method manually after application-specific authentication has completed, to support the post authentication
     * mechanism.
     * </p>
     */
    protected void postAuth() {

        if (OLASSession.get().isUserSet()) {
            Page postAuthPage = OLASSession.get().getPostAuthenticationPage();

            if (postAuthPage != null) {
                OLASSession.get().setPostAuthenticationPage(null);

                throw new RestartResponseException(postAuthPage);
            }
        }
    }

    /**
     * This method is invoked right after the OLAS authentication succeeded.
     * 
     * <p>
     * More specifically, when a page is loaded but no application user is set and the OLAS user is known, this method is called before the
     * constructor of your page.
     * </p>
     */
    protected abstract void onOlasAuthenticated();
}
