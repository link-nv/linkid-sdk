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
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContainerLoginFilter implements Filter {

	private static final Log LOG = LogFactory
			.getLog(ContainerLoginFilter.class);

	private static final String ALREADY_PROCESSED = ContainerLoginFilter.class
			.getName()
			+ ".ALREADY_PROCESSED";

	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpSession httpSession = httpServletRequest.getSession();

		String username = (String) httpSession.getAttribute("username");
		if (null == username) {
			chain.doFilter(request, response);
			return;
		}

		if (true == Boolean.TRUE
				.equals(request.getAttribute(ALREADY_PROCESSED))) {
			chain.doFilter(request, response);
			return;
		}
		request.setAttribute(ALREADY_PROCESSED, Boolean.TRUE);

		LOG.debug("container login " + username + " for "
				+ httpServletRequest.getRequestURL());
		LoginHttpServletRequestWrapper wrapper = new LoginHttpServletRequestWrapper(
				httpServletRequest, username);

		chain.doFilter(wrapper, response);
	}

	public void destroy() {
	}
}
