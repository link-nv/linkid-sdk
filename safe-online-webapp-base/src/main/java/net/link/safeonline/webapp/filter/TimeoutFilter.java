/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.sdk.auth.filter.JAASLoginFilter;
import net.link.safeonline.util.servlet.AbstractInjectionFilter;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Servlet Filter that handles browser timeout events.
 * 
 * <p>
 * The init parameters for this filter are:
 * </p>
 * <ul>
 * <li><code>TimeoutPath</code>: the path to the timeout page.</li>
 * </ul>
 * 
 * @author fcorneli
 * 
 */
public class TimeoutFilter extends AbstractInjectionFilter {

    private static final Log LOG = LogFactory.getLog(TimeoutFilter.class);

    @Init(name = "TimeoutPath")
    private String           timeoutPath;


    public void destroy() {

        LOG.debug("destroy");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        LOG.debug("doFilter");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authenticationTimeout = httpRequest.getParameter("authenticationTimeout");
        LOG.debug("authenticationTimeout=" + authenticationTimeout);
        if (null != authenticationTimeout) {
            LOG.debug("return from timeout authentication webapp");
            chain.doFilter(request, response);
            return;
        }

        String requestedSessionId = httpRequest.getRequestedSessionId();
        if (null == requestedSessionId) {
            /*
             * This means that the user just got here for the first time.
             */
            LOG.debug("no session");
            chain.doFilter(request, response);
            return;
        }
        boolean requestedSessionIdValid = httpRequest.isRequestedSessionIdValid();
        if (true == requestedSessionIdValid) {
            LOG.debug("chain.doFilter");
            chain.doFilter(request, response);
            return;
        }
        /*
         * In this case no corresponding session context for the given requested session Id was found. This could be an
         * indication that the browser caused a timeout on the web application. We detect this via the login cookie.
         * 
         * We also signal the JAASLoginFilter that we redirected. Else it will redirect to the login page.
         */
        if (true == hasCookie(SafeOnlineCookies.LOGIN_COOKIE, httpRequest)) {
            LOG.debug("forwarding to timeout path: " + this.timeoutPath);
            removeCookie(SafeOnlineCookies.LOGIN_COOKIE, httpRequest.getContextPath(), httpRequest, httpResponse);
            JAASLoginFilter.setRedirected(httpRequest.getSession());
            httpResponse.sendRedirect(this.timeoutPath);
            return;
        }
        /*
         * If no login cookie was found, then the browser indeed caused a timeout on the HTTP session, but since the user was not yet logged
         * in, it's not that harmful.
         */
        LOG.debug("non harmful timeout");
        chain.doFilter(request, response);
    }
}
