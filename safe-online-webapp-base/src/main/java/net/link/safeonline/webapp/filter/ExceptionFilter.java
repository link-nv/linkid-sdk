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
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.servlet.AbstractInjectionFilter;
import net.link.safeonline.util.servlet.annotation.Init;

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
public class ExceptionFilter extends AbstractInjectionFilter {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(ExceptionFilter.class);

    @Init(name = "ErrorPage")
    private String            errorPage;


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            ((HttpServletResponse) response).sendRedirect(this.errorPage);
        }
    }

    public void destroy() {

        LOG.debug("destroy");
    }
}
