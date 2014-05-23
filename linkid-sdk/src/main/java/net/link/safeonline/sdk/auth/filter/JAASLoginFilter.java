/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.*;

import net.link.util.logging.Logger;
import java.io.IOException;
import java.util.List;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.*;
import javax.servlet.http.*;
import net.link.util.common.URLUtils;


/**
 * JAAS login servlet filter. This servlet filter takes a userId from the HTTP session and uses it to perform a JAAS login. It also takes
 * care of proper JAAS logout.
 *
 * @author fcorneli
 */
public class JAASLoginFilter implements Filter {

    private static final Logger logger = Logger.get( JAASLoginFilter.class );

    private static final String REDIRECTED_SESSION_ATTRIBUTE = JAASLoginFilter.class.getName() + ".redirected";

    public static final String JAAS_LOGIN_CONTEXT_SESSION_ATTRIB = JAASLoginFilter.class.getName() + ".LOGIN_CONTEXT";

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (!login( httpServletRequest, httpServletResponse ))
            return;

        try {
            chain.doFilter( request, response );
        }
        finally {
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
                logger.dbg( "already redirected" );
                return true;
            }
            String requestPath = URLUtils.concat( config().web().appPath(), request.getServletPath() );

            if (null != loginPath && !loginPath.equals( requestPath ) && !publicPaths.contains( requestPath )) {
                logger.dbg( "redirect to %s, requestPath=%s", loginPath, requestPath );
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

                @Override
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
            loginContext.login();
            request.setAttribute( JAAS_LOGIN_CONTEXT_SESSION_ATTRIB, loginContext );
        }
        catch (SecurityException e) {
            logger.err( e, "During JAAS Login: %s", e.getMessage() );
        }
        catch (LoginException e) {
            logger.err( e, "During JAAS Login: %s", e.getMessage() );
        }
    }

    public static void logout(ServletRequest request) {

        LoginContext loginContext = (LoginContext) request.getAttribute( JAAS_LOGIN_CONTEXT_SESSION_ATTRIB );
        if (loginContext == null)
            return;

        try {
            loginContext.logout();
        }
        catch (LoginException e) {
            logger.err( e, "logout error: %s", e.getMessage() );
        }
    }

    public static void setRedirected(HttpSession session) {

        session.setAttribute( REDIRECTED_SESSION_ATTRIBUTE, true );
    }
}
