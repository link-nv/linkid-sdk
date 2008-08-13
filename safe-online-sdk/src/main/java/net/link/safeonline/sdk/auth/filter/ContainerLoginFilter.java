/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Servlet container filter that sets the servlet container user principal according to the SafeOnline authenticated
 * user.
 *
 * <p>
 * The configuration of this filter should be managed via the <code>web.xml</code> deployment descriptor.
 * </p>
 *
 * @author fcorneli
 *
 */
public class ContainerLoginFilter implements Filter {

    private static final Log    LOG               = LogFactory.getLog(ContainerLoginFilter.class);

    private static final String ALREADY_PROCESSED = ContainerLoginFilter.class.getName() + ".ALREADY_PROCESSED";


    public void init(FilterConfig config) {

        // empty
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        boolean loggedIn = LoginManager.isAuthenticated(httpServletRequest);
        if (false == loggedIn) {
            /*
             * The the user did not authenticate via SafeOnline we cannot set the servlet container user principal yet.
             */
            chain.doFilter(request, response);
            return;
        }

        if (true == Boolean.TRUE.equals(request.getAttribute(ALREADY_PROCESSED))) {
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(ALREADY_PROCESSED, Boolean.TRUE);

        String username = LoginManager.getUsername(httpServletRequest);
        LOG.debug("setting servlet container user principal to " + username);
        LoginHttpServletRequestWrapper wrapper = new LoginHttpServletRequestWrapper(httpServletRequest, username);

        chain.doFilter(wrapper, response);
    }

    public void destroy() {

        // empty
    }
}
