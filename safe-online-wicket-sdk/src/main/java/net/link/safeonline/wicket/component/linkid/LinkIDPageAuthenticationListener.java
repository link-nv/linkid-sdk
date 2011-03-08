/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import net.link.safeonline.wicket.LinkIDSession;
import net.link.safeonline.wicket.annotation.linkid.ForceLogout;
import net.link.safeonline.wicket.annotation.linkid.RequireLogin;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import net.link.util.wicket.util.RedirectToPageException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.*;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;


/**
 * <h2>{@link LinkIDPageAuthenticationListener}</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Apr 2, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public class LinkIDPageAuthenticationListener implements IComponentOnBeforeRenderListener, IComponentInstantiationListener {

    static final Log LOG = LogFactory.getLog( LinkIDPageAuthenticationListener.class );


    public void onInstantiation(Component component) {

        handle( component );
    }

    public void onBeforeRender(Component component) {

        try {
            handle( component );
        } finally {
            // Enforce page requirements for user authentication.
            if (component.getClass().isAnnotationPresent( RequireLogin.class ) && !LinkIDSession.get().isUserSet()) {
                RequireLogin authAnnotation = component.getClass().getAnnotation( RequireLogin.class );
                Class<? extends Page> loginPageClass = authAnnotation.loginPage();

                if (!loginPageClass.equals( Page.class )) {
                    LOG.debug( "[LinkIDWicketAuth] auth required; redirecting unauthenticated user to " + loginPageClass
                               + " for authentication." );

                    LinkIDSession.get().setPostAuthenticationPage( component.getPage().getClass() );
                    throw new RedirectToPageException( loginPageClass );
                }

                LOG.debug( "[LinkIDWicketAuth] auth required; no login page specified for redirection: sending HTTP 401." );
                throw new AbortWithWebErrorCodeException( HttpStatus.SC_UNAUTHORIZED );
            }
        }
    }

    private void handle(Component component) {

        if (component instanceof LinkIDApplicationPage)
            assertAccess( (LinkIDApplicationPage) component );
    }

    private void assertAccess(LinkIDApplicationPage page) {

        try {
            // Check whether page requires forced logout.
            if (page.getClass().isAnnotationPresent( ForceLogout.class ))
                throw new IllegalStateException( "Page " + page.getClass() + " requires forced logout." );

            // Check whether dealing with a linkID Session
            if (Session.get() instanceof LinkIDSession) {

                // Check whether linkID user has changed.
                String wicketLinkID = LinkIDSession.get().findUserLinkID();
                if (LinkIDWicketUtils.isLinkIDAuthenticated()) {
                    if (wicketLinkID != null) {
                        String currentLinkID = LinkIDWicketUtils.findLinkID();
                        if (!wicketLinkID.equals( currentLinkID ))
                            throw new IllegalStateException( "User changed from " + wicketLinkID + " into " + currentLinkID );
                    }

                    // If just logged in using linkID, let application create/push its user onto wicket session.
                    if (!LinkIDSession.get().isUserSet()) {
                        page.onLinkIDAuthenticated();
                        page.postAuth();
                    }
                }
            }
        } catch (IllegalStateException e) {
            // Log out the wicket sessionId.
            LOG.debug( "[LinkIDWicketAuth] Logging out wicket session because: " + e.getMessage() );
            if (!LinkIDSession.get().logout()) {
                // If application indicates logout failed; invalidate the session.

                Session.get().invalidateNow();
                throw new RestartResponseException( Application.get().getApplicationSettings().getAccessDeniedPage() );
            }
        }
    }
}
