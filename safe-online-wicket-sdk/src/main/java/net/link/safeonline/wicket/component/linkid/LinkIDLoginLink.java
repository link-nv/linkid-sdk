/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;


/**
 * <h2>{@link LinkIDLoginLink}<br>
 * <sub>A link that uses the linkID SDK to log a user in through the linkID authentication services.</sub></h2>
 *
 * <p>
 * <i>Sep 22, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public class LinkIDLoginLink extends AbstractLinkIDAuthLink {

    public LinkIDLoginLink(String id) {

        super( id );
    }

    public LinkIDLoginLink(String id, Class<? extends Page> target) {

        super( id, target );
    }

    public void delegate(HttpServletRequest request, HttpServletResponse response, AbstractLinkIDAuthLink link) {

        AuthenticationUtils.login( request, response, newContext());
    }

    /**
     * Override this if you want to provide a custom authentication context.
     *
     * The default context uses the page class and parameters provided by this component to build the URL the user will be sent to after the process has been completed.
     *
     * @return A new logout context.
     */
    protected AuthenticationContext newContext() {

        String targetURL = RequestCycle.get().urlFor( getTarget(), getTargetPageParameters() ).toString();

        return new AuthenticationContext( null, null, null, targetURL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible() {

        return !LinkIDWicketUtils.isLinkIDAuthenticated();
    }
}
