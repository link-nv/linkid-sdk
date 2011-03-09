/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
import net.link.util.j2ee.AbstractInjectionFilter;
import net.link.util.servlet.ErrorMessage;
import net.link.util.servlet.ServletUtils;
import net.link.util.servlet.annotation.Init;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This filter performs the actual login using the identity as received from the SafeOnline authentication web application.
 *
 * @author fcorneli
 */
public class AuthnResponseFilter extends AbstractInjectionFilter {

    private static final Log LOG = LogFactory.getLog( AuthnResponseFilter.class );

    public static final String ERROR_PAGE = "ErrorPage";

    @Init(name = ERROR_PAGE, optional = true)
    private String errorPage;

    public void destroy() {

        LOG.debug( "destroy" );
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        LOG.debug( "doFilter: " + httpRequest.getRequestURL() );

        try {
            AuthnProtocolResponseContext authnResponse = ProtocolManager.findAndValidateAuthnResponse( httpRequest );
            if (null != authnResponse)
                // An authentication response in request that matches an active authentication request.
                LoginManager.set( httpRequest.getSession(), authnResponse.getUserId(), authnResponse.getAttributes(),
                                  authnResponse.getAuthenticatedDevices(), authnResponse.getCertificateChain() );
        } catch (ValidationFailedException e) {
            LOG.error( ServletUtils.redirectToErrorPage( httpRequest, httpResponse, errorPage, null, new ErrorMessage( e ) ) );
        }

        chain.doFilter( request, response );
    }
}
