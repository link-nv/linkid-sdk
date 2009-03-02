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

import net.link.safeonline.util.servlet.AbstractInjectionFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Servlet Filter that handles authentication webapp entries.
 * 
 * If auth-webapp is entered and we have an old invalid session id, just silently create a new session.
 * 
 * @author wvdhaute
 * 
 */
public class EntryFilter extends AbstractInjectionFilter {

    private static final Log LOG = LogFactory.getLog(EntryFilter.class);


    public void destroy() {

        LOG.debug("destroy");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        LOG.debug("auth.doFilter");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestedSessionId = httpRequest.getRequestedSessionId();
        if (null == requestedSessionId) {
            /*
             * This means that the user just got here for the first time.
             */
            LOG.debug("no session");
            chain.doFilter(request, response);
            return;
        }

        if (!httpRequest.isRequestedSessionIdValid()) {
            LOG.debug("invalid session, create new one");
            httpRequest.getSession().invalidate();
            httpRequest.getSession(true);
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
        return;
    }
}
