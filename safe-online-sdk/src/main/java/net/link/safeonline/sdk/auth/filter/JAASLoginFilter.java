/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.config;

import java.io.IOException;
import java.util.List;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.link.util.config.URLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * JAAS login servlet filter. This servlet filter takes a userId from the HTTP session and uses it to perform a JAAS login. It also takes
 * care of proper JAAS logout.
 *
 * @author fcorneli
 */
public class JAASLoginFilter implements Filter {

    private static final Log LOG = LogFactory.getLog( JAASLoginFilter.class );

    private static final String REDIRECTED_SESSION_ATTRIBUTE = JAASLoginFilter.class.getName() + ".redirected";

    public static final String JAAS_LOGIN_CONTEXT_SESSION_ATTRIB = JAASLoginFilter.class.getName() + ".LOGIN_CONTEXT";

    public void init(FilterConfig filterConfig)
            throws ServletException {

    }

    public void destroy() {

        LOG.debug( "destroy" );
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (!login( httpServletRequest, httpServletResponse ))
            return;

        try {
            chain.doFilter( request, response );
        } finally {
            logout( request );
            // processFlushJBossCredentialCache( httpServletRequest );
        }
    }

    private boolean login(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String loginPath = config().jaas().loginPath();
        List<String> publicPaths = config().jaas().publicPaths();

        HttpSession session = request.getSession();
        String userId = LoginManager.findUserId( session );
        if (userId == null) {
            if (session.getAttribute( REDIRECTED_SESSION_ATTRIBUTE ) != null) {
                session.removeAttribute( REDIRECTED_SESSION_ATTRIBUTE );
                LOG.debug( "already redirected" );
                return true;
            }
            String requestPath = URLUtils.concat( config().web().appPath(), request.getServletPath() );

            if (null != loginPath && !loginPath.equals( requestPath ) && !publicPaths.contains( requestPath )) {
                LOG.debug( "redirect to " + loginPath + ", requestPath: " + requestPath );
                setRedirected( session );
                response.sendRedirect( loginPath );
                return false;
            }

            return true;
        }

        login( request, userId );
        return true;
    }

    public static void login(HttpServletRequest request, final String userId) {

        String loginContextName = config().jaas().context();

        try {
            LoginContext loginContext = new LoginContext( loginContextName, new CallbackHandler() {

                public void handle(Callback[] callbacks)
                        throws IOException, UnsupportedCallbackException {

                    for (Callback c : callbacks) {
                        if (c instanceof NameCallback) {
                            NameCallback nc = (NameCallback) c;
                            nc.setName( userId );
                        } else if (c instanceof PasswordCallback) {
                            PasswordCallback pc = (PasswordCallback) c;
                            pc.setPassword( null );
                        } else {
                            throw new UnsupportedCallbackException( c, "Unrecognized Callback" );
                        }
                    }
                }
            } );
            LOG.debug( "login to " + loginContextName + " with " + userId + " for " + request.getRequestURL() );
            loginContext.login();
            request.setAttribute( JAAS_LOGIN_CONTEXT_SESSION_ATTRIB, loginContext );
        } catch (SecurityException e) {
            LOG.warn( "During JAAS Login", e );
        } catch (LoginException e) {
            LOG.error( "During JAAS Login", e );
        }
    }

    public static void logout(ServletRequest request) {

        LoginContext loginContext = (LoginContext) request.getAttribute( JAAS_LOGIN_CONTEXT_SESSION_ATTRIB );
        if (loginContext == null)
            return;

        try {
            LOG.debug( "logout" );
            loginContext.logout();
        } catch (LoginException e) {
            LOG.error( "logout error: " + e.getMessage(), e );
        }
    }

    public static void setRedirected(HttpSession session) {

        session.setAttribute( REDIRECTED_SESSION_ATTRIBUTE, true );
    }
}
