/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.*;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.Protocol;
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        delegate( request, response );
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

        //optional protocol: not specified => SAML2
        Protocol protocol = Protocol.fromString( request.getParameter( RequestConstants.PROTOCOL_PARAM ), Protocol.SAML2 );

        //optional target URL: when login is complete, user will be redirected to this location
        String targetURI = request.getParameter( RequestConstants.TARGETURI_REQUEST_PARAM );

        AuthenticationContext authenticationContext = initAuthenticationContext( request, response, mobileAuthn, mobileAuthnMinimal, mobileForceRegistration,
                targetURI );

        if (null == authenticationContext) {

            authenticationContext = new AuthenticationContext( null, null, targetURI );
            authenticationContext.setMobileAuthentication( mobileAuthn );
            authenticationContext.setMobileAuthenticationMinimal( mobileAuthnMinimal );
            authenticationContext.setMobileForceRegistration( mobileForceRegistration );
        }

        // set the protocol
        authenticationContext.setProtocol( protocol );

        configureAuthenticationContext( authenticationContext, request, response );

        AuthenticationUtils.login( request, response, authenticationContext );
    }

    /**
     * Override this if you want to initialize then authentication context yourself
     */
    @Nullable
    protected AuthenticationContext initAuthenticationContext(final HttpServletRequest request, final HttpServletResponse response, final boolean mobileAuthn,
                                                              final boolean mobileAuthnMinimal, final boolean mobileForceRegistration, final String targetURI) {

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
