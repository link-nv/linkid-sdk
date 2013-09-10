/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import com.lyndir.lhunath.opal.system.logging.Logger;
import net.link.safeonline.wicket.LinkIDSession;
import net.link.safeonline.wicket.annotation.linkid.RequireLogin;
import net.link.util.wicket.component.WicketPage;
import net.link.util.wicket.util.RedirectToPageException;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.WebPage;


/**
 * <h2>{@link LinkIDApplicationPage}<br>
 * <sub>An abstract {@link WebPage} that provides some linkID-specific features to web applications.</sub></h2>
 * <p/>
 * <p>
 * This page takes care of synchronizing the application-level user with the linkID user. This synchronization involves asking the
 * application to create an application-level user after a successful linkID login has been completed, and invalidating the wicket
 * {@link Session} when the linkID user has changed or been logged out.
 * </p>
 * <p/>
 * <p>
 * We also provide a post-authentication mechanism which is used when users go to a page that requires authentication without being
 * authenticated yet. The user is sent to an authentication page (as defined by {@link RequireLogin#loginPage()}), and after a successful
 * authentication the user is sent back to the page he first tried to view. (See {@link #postAuth()})
 * </p>
 * <p/>
 * <p>
 * <i>Dec 15, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class LinkIDApplicationPage extends WicketPage {

    private static final Logger logger = Logger.get( LinkIDApplicationPage.class );

    protected LinkIDApplicationPage() {

    }

    protected LinkIDApplicationPage(PageParameters parameters) {

        super( parameters );
    }

    /**
     * If application created user successfully, perform post-authentication.
     * <p/>
     * <p>
     * Post Authentication will redirect the user back to the page he was on when he was forced to authenticate before being allowed to see
     * the page. If there is no post authentication page set, nothing is done here.
     * </p>
     * <p/>
     * <p>
     * Called after successfully logging in with linkID.<br>
     * You should call this method manually after application-specific authentication has completed, to support the post authentication
     * mechanism.
     * </p>
     */
    protected void postAuth() {

        if (LinkIDSession.get().isUserSet()) {
            Class<? extends Page> postAuthPage = LinkIDSession.get().getPostAuthenticationPage();
            PageParameters postAuthPageParameters = LinkIDSession.get().getPostAuhtenticationParameters() != null? new PageParameters(
                    LinkIDSession.get().getPostAuhtenticationParameters() ): null;

            if (postAuthPage != null) {
                logger.dbg( "[LinkIDWicketAuth] auth completed; triggering post auth, sending user to %s", postAuthPage );

                LinkIDSession.get().setPostAuthenticationPage( null );
                LinkIDSession.get().setPostAuhtenticationParameters( null );

                if (postAuthPageParameters != null)
                    throw new RedirectToPageException( postAuthPage, postAuthPageParameters );
                else
                    throw new RedirectToPageException( postAuthPage );
            }
        }
    }

    /**
     * This method is invoked right after the linkID authentication succeeded.
     * <p/>
     * <p>
     * More specifically, when a page is loaded but no application user is set and the linkID user is known, this method is called before
     * the constructor of your page.
     * </p>
     */
    protected abstract void onLinkIDAuthenticated();
}
