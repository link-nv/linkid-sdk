package net.link.safeonline.sdk.auth.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.*;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.servlet.AbstractLinkIDInjectionServlet;
import org.jetbrains.annotations.Nullable;


/**
 * A simple Servlet to initiate the login procedure on LinkID (i.e. this servlet represents a 'protected resource' which requires
 * authentication).
 * Landing on this servlet will return a redirect url to LinkID authentication service, an authentication Request, and possibly additional
 * parameters.
 * <p/>
 * User: sgdesmet
 * Date: 03/11/11
 * Time: 10:21
 */
public class InitiateLoginServlet extends AbstractLinkIDInjectionServlet {

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        delegate( request, response );
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        delegate( request, response );
    }

    /**
     * create authentication context and start login
     */
    public void delegate(final HttpServletRequest request, final HttpServletResponse response) {

        boolean mobileAuthn = null != request.getParameter( RequestConstants.MOBILE_AUTHN_REQUEST_PARAM );
        boolean mobileAuthnMinimal = null != request.getParameter( RequestConstants.MOBILE_AUTHN_MINIMAL_REQUEST_PARAM );
        boolean mobileForceRegistration = null != request.getParameter( RequestConstants.MOBILE_FORCE_REG_REQUEST_PARAM );

        //optional target URL: when login is complete, user will be redirected to this location
        String targetURI = request.getParameter( RequestConstants.TARGETURI_REQUEST_PARAM );

        // optional login mode
        String modeParam = request.getParameter( RequestConstants.LOGINMODE_REQUEST_PARAM );
        LoginMode mode = LoginMode.fromString( modeParam );

        // optional force registration
        StartPage startPage = StartPage.fromString( request.getParameter( RequestConstants.START_PAGE_REQUEST_PARAM ), StartPage.NONE );

        AuthenticationContext authenticationContext = initAuthenticationContext( request, response, mobileAuthn, mobileAuthnMinimal, mobileForceRegistration,
                targetURI, mode, startPage );

        if (null == authenticationContext) {

            authenticationContext = new AuthenticationContext( null, null, null, targetURI, mode );
            authenticationContext.setStartPage( startPage );
            authenticationContext.setMobileAuthentication( mobileAuthn );
            authenticationContext.setMobileAuthenticationMinimal( mobileAuthnMinimal );
            authenticationContext.setMobileForceRegistration( mobileForceRegistration );
        }

        configureAuthenticationContext( authenticationContext, request, response );

        AuthenticationUtils.login( request, response, authenticationContext );
    }

    /**
     * Override this if you want to initialize then authentication context yourself
     */
    @Nullable
    protected AuthenticationContext initAuthenticationContext(final HttpServletRequest request, final HttpServletResponse response, final boolean mobileAuthn,
                                                              final boolean mobileAuthnMinimal, final boolean mobileForceRegistration, final String targetURI,
                                                              final LoginMode mode, final StartPage startPage) {

        return null;
    }

    /**
     * Override this if you want to configure the authentication context
     */
    protected void configureAuthenticationContext(final AuthenticationContext authenticationContext, final HttpServletRequest request,
                                                  final HttpServletResponse response) {

        // do nothing
    }
}
