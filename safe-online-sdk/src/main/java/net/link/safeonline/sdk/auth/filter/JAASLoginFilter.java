/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.UsernamePasswordHandler;


/**
 * JAAS login servlet filter. This servlet filter takes a userId from the HTTP session and uses it to perform a JAAS login. It also takes
 * care of proper JAAS logout.
 * 
 * <p>
 * When running within a JBoss Application Server, EJB components can set a session attribute called
 * {@link #FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME} to flush the security domain credentials of the caller principal. The value of this
 * attribute is the name of the security domain for which to flush the credential cache. This can be useful for EJB components that make
 * changes to the credentials of the caller principal.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class JAASLoginFilter implements Filter {

    private static final Log    LOG                               = LogFactory.getLog(JAASLoginFilter.class);

    private static final String REDIRECTED_SESSION_ATTRIBUTE      = JAASLoginFilter.class.getName() + ".redirected";

    public static final String  JAAS_LOGIN_CONTEXT_SESSION_ATTRIB = JAASLoginFilter.class.getName() + ".LOGIN_CONTEXT";

    public static final String  LOGIN_CONTEXT_PARAM               = "LoginContextName";

    public static final String  LOGIN_PATH_PARAM                  = "LoginPath";

    public static final String  UNAUTHENTICATED_PATH_PARAM        = "UnauthenticatedPath";

    /**
     * The default JAAS login context is 'client-login'. This is what JBoss AS expects of EJB clients to use for login.
     */
    private static final String DEFAULT_LOGIN_CONTEXT             = "client-login";

    private String              loginContextName;

    private String              loginPath;

    private List<String>        unauthenticatedPaths;


    public void init(FilterConfig config) {

        this.loginContextName = getInitParameter(config, LOGIN_CONTEXT_PARAM, DEFAULT_LOGIN_CONTEXT);
        LOG.debug("JAAS login context: " + this.loginContextName);
        this.loginPath = getInitParameter(config, LOGIN_PATH_PARAM, null);
        LOG.debug("LoginPath: " + this.loginPath);
        this.unauthenticatedPaths = getUnauthenticatedPaths(config);
    }

    private String getInitParameter(FilterConfig config, String param, String defaultValue) {

        String value = config.getInitParameter(param);
        if (null == value) {
            value = defaultValue;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private List<String> getUnauthenticatedPaths(FilterConfig config) {

        List<String> paths = new LinkedList<String>();
        Enumeration<String> paramNames = config.getInitParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();
            if (param.startsWith(UNAUTHENTICATED_PATH_PARAM)) {
                paths.add(config.getInitParameter(param));
            }
        }
        return paths;

    }

    public void destroy() {

        LOG.debug("destroy");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (!login(httpServletRequest, httpServletResponse))
            return;
        try {
            chain.doFilter(request, response);
        } finally {
            logout(request);
            processFlushJBossCredentialCache(httpServletRequest);
        }
    }

    private boolean login(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOG.debug("login");
        String userId = LoginManager.findUserId(request);
        if (userId == null) {
            HttpSession session = request.getSession();
            if (session.getAttribute(REDIRECTED_SESSION_ATTRIBUTE) != null) {
                session.removeAttribute(REDIRECTED_SESSION_ATTRIBUTE);
                LOG.debug("already redirected");
                return true;
            }
            if (null != this.loginPath && !this.loginPath.equals(request.getRequestURL().toString())
                    && !this.unauthenticatedPaths.contains(request.getRequestURL().toString())) {
                LOG.debug("redirect to " + this.loginPath);
                session.setAttribute(REDIRECTED_SESSION_ATTRIBUTE, true);
                response.sendRedirect(this.loginPath);
                return false;
            }
            return true;
        }
        UsernamePasswordHandler handler = new UsernamePasswordHandler(userId, null);
        try {
            LoginContext loginContext = new LoginContext(this.loginContextName, handler);
            LOG.debug("login to " + this.loginContextName + " with " + userId + " for " + request.getRequestURL());
            loginContext.login();
            request.setAttribute(JAAS_LOGIN_CONTEXT_SESSION_ATTRIB, loginContext);
        } catch (LoginException e) {
            LOG.error("login error: " + e.getMessage(), e);
        }
        return true;
    }

    private void logout(ServletRequest request) {

        LoginContext loginContext = (LoginContext) request.getAttribute(JAAS_LOGIN_CONTEXT_SESSION_ATTRIB);
        if (loginContext == null)
            return;
        try {
            LOG.debug("logout");
            loginContext.logout();
        } catch (LoginException e) {
            LOG.error("logout error: " + e.getMessage(), e);
        }
    }


    public static final String FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME = "FlushJBossCredentialCache";


    private void processFlushJBossCredentialCache(HttpServletRequest request) throws ServletException {

        HttpSession session = request.getSession(false);
        /*
         * We could trigger here an java.lang.IllegalStateException: Cannot create a session after the response has been
         * committed.
         * 
         * So be careful when retrieving the session.
         */
        if (null == session)
            return;
        String securityDomain = (String) session.getAttribute(FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME);
        /*
         * The EJB components can set this attribute via JACC.
         */
        if (null == securityDomain)
            return;
        String username = LoginManager.getUserId(request);
        LOG.debug("trying to flush JBoss credential cache for " + username + " on security domain " + securityDomain);
        try {
            flushCredentialCache(username, securityDomain);
        } finally {
            session.removeAttribute(FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME);
        }
    }

    private void flushCredentialCache(String login, String securityDomain) {

        LOG.debug("flush credential cache for " + login + " on security domain " + securityDomain);
        Principal user = new SimplePrincipal(login);
        ObjectName jaasMgr;
        try {
            jaasMgr = new ObjectName("jboss.security:service=JaasSecurityManager");
        } catch (MalformedObjectNameException e) {
            String msg = "ObjectName error: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("NPE: " + e.getMessage(), e);
        }
        Object[] params = { securityDomain, user };
        String[] signature = { String.class.getName(), Principal.class.getName() };
        MBeanServer server = MBeanServerLocator.locateJBoss();
        try {
            server.invoke(jaasMgr, "flushAuthenticationCache", params, signature);
        } catch (InstanceNotFoundException e) {
            String msg = "instance not found: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (MBeanException e) {
            String msg = "mbean error: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (ReflectionException e) {
            String msg = "reflection error: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public static void setRedirected(HttpSession session) {

        session.setAttribute(REDIRECTED_SESSION_ATTRIBUTE, true);
    }
}
