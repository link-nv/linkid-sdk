/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.*;
import net.link.safeonline.sdk.auth.servlet.InitiateLoginServlet;
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
@SuppressWarnings("UnusedDeclaration")
public class LinkIDJavaScriptLoginLink extends AbstractLinkIDAuthJSLink {

    protected LoginMode loginMode;
    protected StartPage startPage;

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

    /**
     * Set the login style (redirect, popup window, modal window). Only used if this parameter has not already been set by linkid.js
     */
    public void setLoginMode(final LoginMode loginMode) {

        this.loginMode = loginMode;
    }

    public LoginMode getLoginMode() {

        return loginMode;
    }

    /*
     * Skip the "do you have an account page" and go straight to register or authenticate even when deflowered.
     */

    public StartPage getStartPage() {

        return startPage;
    }

    public void setStartPage(final StartPage startPage) {

        this.startPage = startPage;
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
        boolean mobileAuthentication = null != request.getParameter( RequestConstants.MOBILE_AUTHN_REQUEST_PARAM );
        boolean mobileAuthenticationMinimal = null != request.getParameter( RequestConstants.MOBILE_AUTHN_MINIMAL_REQUEST_PARAM );

        String targetURL = request.getParameter( RequestConstants.TARGETURI_REQUEST_PARAM );
        String modeParam = request.getParameter( RequestConstants.LOGINMODE_REQUEST_PARAM );
        LoginMode mode = LoginMode.fromString( modeParam );

        if (targetURL == null) {
            targetURL = RequestCycle.get().urlFor( targetPage, targetPageParameters ).toString();
        }

        if (mode == null) {
            mode = loginMode;
        }

        AuthenticationContext authenticationContext = new AuthenticationContext( null, null, null, targetURL, mode );
        authenticationContext.setStartPage( startPage );
        authenticationContext.setMobileAuthentication( mobileAuthentication );
        authenticationContext.setMobileAuthenticationMinimal( mobileAuthenticationMinimal );
        return authenticationContext;
    }

    @Override
    public boolean isVisible() {

        return !LinkIDWicketUtils.isLinkIDAuthenticated();
    }

    /**
     * @return URL to use for initiating a linkID login request ( {@link InitiateLoginServlet}.
     *         If {@code null} the default URL in the linkid login js ( /startlogin ) will be used.
     */
    @Nullable
    public String findLoginUrl() {

        return null;
    }

    /**
     * @return URL to redirect to when the linkID authentication process has completed.
     *         If {@code null} will redirect back to current location
     */
    @Nullable
    public String findRedirectToOnComplete() {

        return null;
    }
}
