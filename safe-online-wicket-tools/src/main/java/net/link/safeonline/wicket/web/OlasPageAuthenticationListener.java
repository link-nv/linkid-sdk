/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;


/**
 * <h2>{@link OlasPageAuthenticationListener}<br>
 * <sub>[in short] (TODO).</sub></h2>
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
public class OlasPageAuthenticationListener implements IComponentOnBeforeRenderListener {

    static final Log LOG = LogFactory.getLog(OlasPageAuthenticationListener.class);


    /**
     * {@inheritDoc}
     */
    public void onBeforeRender(Component component) {

        if (!(component instanceof OlasApplicationPage))
            return;

        OlasApplicationPage page = (OlasApplicationPage) component;

        try {
            // Check whether page requires forced logout.
            if (page.getClass().isAnnotationPresent(ForceLogout.class))
                throw new IllegalStateException("Page " + page.getClass() + " requires forced logout.");

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
                        page.onOlasAuthenticated();
                        page.postAuth();
                    }
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
            if (page.getClass().isAnnotationPresent(RequireLogin.class) && !OLASSession.get().isUserSet()) {
                RequireLogin authAnnotation = page.getClass().getAnnotation(RequireLogin.class);
                Class<? extends Page> loginPage = authAnnotation.loginPage();

                if (!loginPage.equals(Page.class)) {
                    LOG.debug("[OlasWicketAuth] auth required; redirecting unauthenticated user to " + loginPage + " for authentication.");

                    OLASSession.get().setPostAuthenticationPage(page.getClass());
                    throw new RestartResponseException(loginPage);
                }

                LOG.debug("[OlasWicketAuth] auth required; no login page specified for redirection: sending HTTP 401.");
                throw new AbortWithWebErrorCodeException(HttpStatus.SC_UNAUTHORIZED);
            }
        }
    }
}
