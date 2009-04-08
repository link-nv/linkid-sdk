/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;


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


    public OlasApplicationPage() {

        super();
    }

    public OlasApplicationPage(PageParameters parameters) {

        super(parameters);
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
