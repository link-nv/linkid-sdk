/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * authenticated yet. The user is sent to an authentication page (as defined by {@link RequireLogin#loginPage()}), and after a successful
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

    static final Log LOG = LogFactory.getLog(OlasApplicationPage.class);

    {
        try {
            // Check whether page requires forced logout.
            if (getClass().isAnnotationPresent(ForceLogout.class))
                throw new IllegalStateException("Page " + getClass() + " requires forced logout.");

            // Check whether dealing with an OLAS Session
            if (Session.get() instanceof OLASSession) {

                // Check whether OLAS user has changed.
                String wicketOlasId = OLASSession.get().getUserOlasId();
                if (WicketUtil.isOlasAuthenticated()) {
                    if (wicketOlasId != null) {
                        String currentOlasId = WicketUtil.findOlasId();
                        if (!wicketOlasId.equals(currentOlasId))
                            throw new IllegalStateException("User changed from " + wicketOlasId + " into " + currentOlasId);
                    }

                    // If just logged in using OLAS, let application create/push its user onto wicket session.
                    if (!OLASSession.get().isUserSet()) {
                        onOlasAuthenticated();
                        postAuth();
                    }
                }

                else if (wicketOlasId != null) {
                    // No OLAS user on session, but wicket session says an OLAS user is logged in..
                    // Either the application allowed an OLAS user to login without using OLAS (eg. demo-bank),
                    // or the OLAS user logged itself out but the wicket session wasn't cleaned up properly.
                    // In this latter case, the logged out user's privileges leak to the next user.
                    // (TODO) Not sure what to do. (SOS-373)
                }
            }
        }

        catch (IllegalStateException e) {
            // Log out the wicket session.
            LOG.debug("[OlasWicketAuth] Logging out wicket session because: " + e.getMessage());
            if (!OLASSession.get().logout()) {
                // If application indicates logout failed; invalidate the session.

                Session.get().invalidateNow();
                throw new AbortWithWebErrorCodeException(HttpStatus.SC_UNAUTHORIZED);
            }
        }

        finally {
            // Enforce page requirements for user authentication.
            if (getClass().isAnnotationPresent(RequireLogin.class) && !OLASSession.get().isUserSet()) {
                RequireLogin authAnnotation = getClass().getAnnotation(RequireLogin.class);
                Class<? extends Page> loginPage = authAnnotation.loginPage();

                if (!loginPage.equals(Page.class)) {
                    LOG.debug("[OlasWicketAuth] auth required; redirecting unauthenticated user to " + loginPage + " for authentication.");

                    OLASSession.get().setPostAuthenticationPage(getClass());
                    throw new RestartResponseException(loginPage);
                }

                LOG.debug("[OlasWicketAuth] auth required; no login page specified for redirection: sending HTTP 401.");
                throw new AbortWithWebErrorCodeException(HttpStatus.SC_UNAUTHORIZED);
            }
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
            Class<? extends Page> postAuthPage = OLASSession.get().getPostAuthenticationPage();

            if (postAuthPage != null) {
                LOG.debug("[OlasWicketAuth] auth completed; triggering post auth, sending user to " + postAuthPage);

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
