/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.LogoutContext;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import net.link.util.wicket.util.RedirectToPageException;
import net.link.util.wicket.util.WicketUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;


/**
 * <h2>{@link LinkIDLoginLink}<br> <sub>A link that uses the linkID SDK to log a user out of this application and all other applications in
 * its SSO pool through the linkID authentication services.</sub></h2>
 *
 * <p> <i>Sep 22, 2008</i> </p>
 *
 * @author lhunath
 */
public class LinkIDLogoutLink extends AbstractLinkIDAuthLink {

    private static Log LOG = LogFactory.getLog( LinkIDLogoutLink.class );

    private boolean logoutEnabled;

    public LinkIDLogoutLink(String id) {

        super( id );
        logoutEnabled = true;
    }

    public LinkIDLogoutLink(String id, boolean logoutEnabled) {

        super( id );
        this.logoutEnabled = logoutEnabled;
    }

    public LinkIDLogoutLink(String id, Class<? extends Page> target) {

        super( id, target );
    }

    public LinkIDLogoutLink(String id, Class<? extends Page> target, boolean logoutEnabled) {

        super( id, target );
        this.logoutEnabled = logoutEnabled;
    }

    public boolean isLogoutEnabled() {

        return logoutEnabled;
    }

    public void delegate(final Class<? extends Page> target, final PageParameters targetPageParameters) {

        boolean redirected = false;
        if (LoginManager.isAuthenticated( WicketUtils.getHttpSession() ))
            redirected = AuthenticationUtils.logout( WicketUtils.getServletRequest(), WicketUtils.getServletResponse(),
                    newContext( target, targetPageParameters ) );

        if (!redirected) {
            LOG.debug( "Logout handled locally; invalidating sessionId." );
            Session.get().invalidateNow();

            throw new RedirectToPageException( target, targetPageParameters );
        }
    }

    /**
     * Override this if you want to provide a custom logout context.
     *
     * The default context uses the page class and parameters provided by this component to build the URL the user will be sent to after the
     * process has been completed.
     *
     * @param target               The page where the user should end up after delegation.
     * @param targetPageParameters The parameters to pass to the page on construction.
     *
     * @return A new logout context.
     */
    protected LogoutContext newContext(final Class<? extends Page> target, final PageParameters targetPageParameters) {

        String targetURL = RequestCycle.get().urlFor( target, targetPageParameters ).toString();

        return new LogoutContext( null, null, targetURL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible() {

        return logoutEnabled && LinkIDWicketUtils.isLinkIDAuthenticated();
    }
}