/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.SafeOnlineConfig;
import net.link.util.servlet.AbstractInjectionFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;


/**
 * SafeOnline Authentication Request Filter. This filter can be used by servlet container based web applications for authentication via
 * SafeOnline. This filter initiates the authentication request towards the SafeOnline authentication web application. The handling of the
 * authentication response is done via the {@link AuthnResponseFilter}.
 *
 * <p>
 * The configuration of this filter should be managed via the <code>web.xml</code> deployment descriptor.
 * </p>
 *
 * <p>
 * If an application wishes to communicate to the linkID authentication webapp the language to be used, the session parameter
 * <code>Language</code> needs to be set containing as value the {@link Locale} of the language.
 * </p>
 *
 * @author fcorneli
 * @see AuthnResponseFilter
 */
public class AuthnRequestFilter extends AbstractInjectionFilter {

    private static final Log LOG = LogFactory.getLog( AuthnRequestFilter.class );

    /**
     * Bind your session tracking identifier to the HTTP session attribute with this name if you want to do session tracking.
     */
    public static final String SESSION_TRACKING_ATTRIBUTE = "SessionTracking";


    /**
     * {@inheritDoc}
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        LOG.debug( "doFilter" );
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (LoginManager.isAuthenticated( httpRequest.getSession() ))
            chain.doFilter( httpRequest, httpResponse );
        else
            AuthenticationUtils.login( httpRequest, httpResponse, newContext( httpRequest ) );
    }

    /**
     * Override this method if you want to provide a custom authentication context for initiating authentication requests.
     *
     * The default implementation creates a context based on configuration defaults (see {@link SafeOnlineConfig}), which doesn't force authentication and returns the user to the page where authentication was initiated after the process has completed.
     *
     * @param request The request that has caused the need for authentication.
     *
     * @return The authentication context that specifies the configuration of the authentication process.
     */
    @NotNull
    protected AuthenticationContext newContext(HttpServletRequest request) {

        return new AuthenticationContext( null, null, null, false, null, (String) request.getAttribute(
                SESSION_TRACKING_ATTRIBUTE ), null, null, request.getRequestURL().toString() );
    }

    public void destroy() {

        LOG.debug( "destroy" );
    }
}
