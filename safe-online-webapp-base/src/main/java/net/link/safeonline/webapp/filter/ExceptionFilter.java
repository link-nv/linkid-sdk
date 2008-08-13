/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Exception filter that handles all GUI exceptions.
 *
 * <p>
 * The init parameters for this filter are:
 * </p>
 * <ul>
 * <li><code>ErrorPage</code>: the path to the error page.</li>
 * </ul>
 *
 * @author wvdhaute
 *
 */
public class ExceptionFilter implements Filter {

    private static final long   serialVersionUID      = 1L;

    private static final Log    LOG                   = LogFactory.getLog(ExceptionFilter.class);

    private static final String INIT_PARAM_ERROR_PAGE = "ErrorPage";

    private String              errorPage;


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            ((HttpServletResponse) response).sendRedirect(this.errorPage);
        }
    }

    public void init(FilterConfig config) throws ServletException {

        LOG.debug("init");
        this.errorPage = getInitParameter(config, INIT_PARAM_ERROR_PAGE);
        LOG.debug("errorpage path: " + this.errorPage);
    }

    private String getInitParameter(FilterConfig config, String parameterName) throws UnavailableException {

        String value = config.getInitParameter(parameterName);
        if (null == value)
            throw new UnavailableException("missing init parameter: " + parameterName);
        return value;
    }

    public void destroy() {

        LOG.debug("destroy");
    }
}
