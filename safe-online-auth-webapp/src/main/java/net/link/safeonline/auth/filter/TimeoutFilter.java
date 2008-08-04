/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.Timeout;
import net.link.safeonline.util.ee.BufferedServletResponseWrapper;
import net.link.safeonline.util.servlet.AbstractInjectionFilter;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet Filter that handles authentication browser timeout events.
 * 
 * This filter will set ( if not already ) a cookie containing the applicationId
 * if the current session is valid. If the session is invalid, it will check for
 * such a cookie, fetch the {@link LoginManager#APPLICATION_ID_ATTRIBUTE} and
 * lookup the application URL. If such an url can be retrieved, it will display
 * the timeout page as configured with the <code>TimeoutPath</code> containing
 * this URL. Else the URL will just be omitted.
 * 
 * <p>
 * The init parameters for this filter are:
 * </p>
 * <ul>
 * <li><code>TimeoutPath</code>: the path to the timeout page.</li>
 * </ul>
 * 
 * @author wvdhaute
 * 
 */
public class TimeoutFilter extends AbstractInjectionFilter {

    private static final Log    LOG         = LogFactory
                                                    .getLog(TimeoutFilter.class);

    private static final String COOKIE_PATH = "/olas-auth/";

    @Init(name = "TimeoutPath")
    private String              timeoutPath;


    public void destroy() {

        LOG.debug("destroy");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        LOG.debug("auth.doFilter");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestedSessionId = httpRequest.getRequestedSessionId();
        if (null == requestedSessionId) {
            /*
             * This means that the user just got here for the first time.
             */
            LOG.debug("no session");
            chain.doFilter(request, response);
            return;
        }
        boolean requestedSessionIdValid = httpRequest
                .isRequestedSessionIdValid();
        if (true == requestedSessionIdValid) {
            /*
             * We wrap the response since we need to be able to add cookies
             * after the body has been committed.
             */
            BufferedServletResponseWrapper timeoutResponseWrapper = new BufferedServletResponseWrapper(
                    httpResponse);
            LOG.debug("auth.chain.doFilter");
            chain.doFilter(request, timeoutResponseWrapper);
            /*
             * This means that the servlet container found a matching session
             * context for the requested session Id.
             */
            HttpSession session = httpRequest.getSession();
            String applicationId = LoginManager.findApplication(session);
            if (null != applicationId) {
                setCookie(Timeout.APPLICATION_COOKIE, applicationId,
                        COOKIE_PATH, httpResponse);
            }
            /*
             * Remove the possible timeout cookie, add entry cookie to prevent
             * timing out again on first entry after a previous timeout.
             */
            removeCookie(Timeout.TIMEOUT_COOKIE, COOKIE_PATH, httpRequest,
                    httpResponse);
            addCookie(Timeout.ENTRY_COOKIE, "", COOKIE_PATH, httpRequest,
                    httpResponse);
            timeoutResponseWrapper.commit();
            return;
        }
        if (hasCookie(Timeout.TIMEOUT_COOKIE, httpRequest)) {
            /*
             * In this case we already redirected to the timeout page.
             */
            LOG.debug("invalid session, on timeout page");
            chain.doFilter(request, response);
            return;
        }

        /*
         * In this case no corresponding session context for the given requested
         * session Id was found. This could be an indication that the browser
         * caused a timeout on the web application. We should redirect to the
         * timeout path, add the timeout cookie to not get in an infinite
         * timeout redirect loop, and remove the entry cookie.
         */
        if (hasCookie(Timeout.ENTRY_COOKIE, httpRequest)) {
            LOG.debug("forwarding to timeout path: " + this.timeoutPath);
            addCookie(Timeout.TIMEOUT_COOKIE, "", COOKIE_PATH, httpRequest,
                    httpResponse);
            removeCookie(Timeout.ENTRY_COOKIE, COOKIE_PATH, httpRequest,
                    httpResponse);
            httpResponse.sendRedirect(this.timeoutPath);
            return;
        }

        /*
         * No entry cookie so user just landed here, after a previous timeout
         * session, dont again redirect the poor user to the timeout page.
         */
        LOG.debug("first entry after previous timeout");
        chain.doFilter(request, response);
        return;
    }
}
