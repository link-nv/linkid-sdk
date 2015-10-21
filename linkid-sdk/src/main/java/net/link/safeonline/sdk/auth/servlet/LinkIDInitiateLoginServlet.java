/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.LinkIDRequestConstants;
import net.link.safeonline.sdk.auth.util.LinkIDAuthenticationUtils;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.configuration.LinkIDProtocol;
import net.link.safeonline.sdk.servlet.LinkIDAbstractInjectionServlet;
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
public class LinkIDInitiateLoginServlet extends LinkIDAbstractInjectionServlet {

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

        //optional protocol: not specified => SAML2
        LinkIDProtocol protocol = LinkIDProtocol.fromString( request.getParameter( LinkIDRequestConstants.PROTOCOL_PARAM ), LinkIDProtocol.SAML2 );

        //optional target URL: when login is complete, user will be redirected to this location
        String targetURI = request.getParameter( LinkIDRequestConstants.TARGETURI_REQUEST_PARAM );

        // optional language
        String language = request.getParameter( LinkIDRequestConstants.LANGUAGE_REQUEST_PARAM );

        LinkIDAuthenticationContext linkIDAuthenticationContext = initAuthenticationContext( request, response, targetURI );

        if (null == linkIDAuthenticationContext) {

            linkIDAuthenticationContext = new LinkIDAuthenticationContext( null, null, targetURI );
        }

        if (null != language) {
            linkIDAuthenticationContext.setLanguage( new Locale( language ) );
        }

        // set the protocol
        linkIDAuthenticationContext.setProtocol( protocol );

        configureAuthenticationContext( linkIDAuthenticationContext, request, response );

        LinkIDAuthenticationUtils.login( request, response, linkIDAuthenticationContext );
    }

    /**
     * Override this if you want to initialize then authentication context yourself
     */
    @Nullable
    protected LinkIDAuthenticationContext initAuthenticationContext(final HttpServletRequest request, final HttpServletResponse response,
                                                                    final String targetURI) {

        return null;
    }

    /**
     * Override this if you want to configure the authentication context
     */
    protected void configureAuthenticationContext(final LinkIDAuthenticationContext linkIDAuthenticationContext, final HttpServletRequest request,
                                                  final HttpServletResponse response) {

        // do nothing
    }
}
