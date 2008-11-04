/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.service.AuthorizationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimplePrincipal;


/**
 * Servlet Container login filter. This filter provides perceived servlet container security. This means that the servlet web application
 * that is applying this filter will see meaningful values for the request.getUserPrincipal and request.isUserInRole methods. This filter
 * does not provide web resource protection itself.
 * 
 * @see <a href="http://securityfilter.sourceforge.net/test">SecurityFilter</a>
 * 
 * @author fcorneli
 * 
 */
public class ServletLoginFilter implements Filter {

    private static final Log     LOG = LogFactory.getLog(ServletLoginFilter.class);

    private AuthorizationService authorizationService;


    public void destroy() {

        LOG.debug("destroy");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        LOG.debug("doFilter");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        HttpSession session = httpServletRequest.getSession();
        String userId = (String) session.getAttribute(LoginManager.USERID_SESSION_ATTRIBUTE);
        if (null == userId) {
            chain.doFilter(request, response);
            return;
        }

        // TODO: cache roles in http request context
        Set<String> roles = getAuthorizationService().getRoles(userId);

        Principal userPrincipal = new SimplePrincipal(userId);
        LoginHttpServletRequestWrapper loginHttpServletRequestWrapper = new LoginHttpServletRequestWrapper(httpServletRequest,
                userPrincipal, roles);

        chain.doFilter(loginHttpServletRequestWrapper, response);
    }

    public AuthorizationService getAuthorizationService() {

        if (this.authorizationService != null)
            return this.authorizationService;

        LOG.debug("init authorizationService");
        try {
            return this.authorizationService = EjbUtils.getEJB(AuthorizationService.JNDI_BINDING, AuthorizationService.class);
        } catch (RuntimeException e) {
            LOG.error("authorization service lookup failure", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void init(FilterConfig filterConfig)
            throws ServletException {

    }
}
