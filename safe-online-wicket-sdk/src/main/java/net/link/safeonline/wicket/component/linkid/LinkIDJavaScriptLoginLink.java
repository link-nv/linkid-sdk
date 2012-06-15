/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.LoginMode;
import net.link.safeonline.sdk.api.auth.RequestConstants;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import org.apache.wicket.*;
import org.apache.wicket.protocol.http.WebRequest;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link LinkIDJavaScriptLoginLink}<br> <sub>A link that uses the linkID SDK to log a user in through the linkID authentication
 * services.
 * Needs 'linkid.login.js' on the page, but this class can add it itself.
 * <p/>
 * </sub></h2>
 * <p/>
 * <p> <i>Nov 24, 2011</i> </p>
 *
 * @author sgdesmet
 */
public class LinkIDJavaScriptLoginLink extends AbstractLinkIDAuthJSLink {

    protected boolean addJS;
    protected LoginMode loginMode = null;

    /**
     * Constructor. Adds 'linkid.login.js' to the page.
     */
    public LinkIDJavaScriptLoginLink(String id) {

        this( id, null, true );
    }

    /**
     * Constructor. Adds 'linkid.login.js' to the page.
     */
    public LinkIDJavaScriptLoginLink(String id, Class<? extends Page> target) {

        this( id, target, true );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.login.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    public LinkIDJavaScriptLoginLink(String id, boolean addJS) {

        this( id, null, addJS );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.login.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    public LinkIDJavaScriptLoginLink(String id, @Nullable Class<? extends Page> target, boolean addJS) {

        super( id, "login", target, addJS );
    }

    @Override
    public void delegate(final HttpServletRequest request, final HttpServletResponse response, final Class<? extends Page> target,
                         final PageParameters targetPageParameters) {

        AuthenticationUtils.login( request, response, newContext( target, targetPageParameters ) );
    }

    /**
     * Override this if you want to provide a custom authentication context.
     * <p/>
     * The default context uses the page class and parameters provided by this component to build the URL the user will be sent to after
     * the
     * process has been completed.
     *
     * @param targetPage           The page where the user should end up after delegation.
     * @param targetPageParameters The parameters to pass to the page on construction.
     *
     * @return A new logout context.
     */
    protected AuthenticationContext newContext(final Class<? extends Page> targetPage, final PageParameters targetPageParameters) {

        WebRequest request = getWebRequest();
        String targetURL = request.getParameter( RequestConstants.TARGETURI_REQUEST_PARAM );
        String modeParam = request.getParameter( RequestConstants.LOGINMODE_REQUEST_PARAM );
        LoginMode mode = LoginMode.fromString( modeParam );

        if (targetURL == null) {
            targetURL = RequestCycle.get().urlFor( targetPage, targetPageParameters ).toString();
        }

        if (mode == null) {
            mode = loginMode;
        }

        return new AuthenticationContext( null, null, null, targetURL, mode );
    }

    @Override
    public boolean isVisible() {

        return !LinkIDWicketUtils.isLinkIDAuthenticated();
    }
}
