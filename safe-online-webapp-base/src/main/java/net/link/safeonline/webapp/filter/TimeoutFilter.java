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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet Filter that handles browser timeout events.
 * 
 * @author fcorneli
 * 
 */
public class TimeoutFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(TimeoutFilter.class);

	private String timeoutPath;

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestedSessionId = httpRequest.getRequestedSessionId();
		if (null == requestedSessionId) {
			/*
			 * This means that the user just got here for the first time.
			 */
			chain.doFilter(request, response);
			return;
		}
		boolean requestedSessionIdValid = httpRequest
				.isRequestedSessionIdValid();
		if (true == requestedSessionIdValid) {
			/*
			 * This means that the servlet container found a matching session
			 * context for the requested session Id.
			 */
			chain.doFilter(request, response);
			return;
		}
		/*
		 * In this case no corresponding session context for the given requested
		 * session Id was found. This is an indication that the browser caused a
		 * timeout on the web application.
		 */
		LOG.debug("forwaring to timeout path: " + this.timeoutPath);

		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher(this.timeoutPath);
		requestDispatcher.forward(request, response);
	}

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.timeoutPath = config.getInitParameter("TimeoutPath");
		if (null == this.timeoutPath) {
			throw new UnavailableException("missing TimeoutPath init parameter");
		}
	}
}
