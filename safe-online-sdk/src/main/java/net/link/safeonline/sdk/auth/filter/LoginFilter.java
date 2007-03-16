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

/**
 * This filter performs the actual login using the identity as received from the
 * SafeOnline authentication web application.
 * 
 * @author fcorneli
 * 
 */
public class LoginFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(LoginFilter.class);

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		LOG.debug("doFilter: " + httpRequest.getRequestURL());
		HttpSession session = httpRequest.getSession();

		String paramUsername = httpRequest.getParameter("username");
		/*
		 * TODO: retrieve assertion and verify integrity.
		 * 
		 * Later on we will receive an assertion Id here. Using the assertion Id
		 * we should go to the SafeOnline Authentication Web Service to retrieve
		 * the SAML assertion. After checking the integrity of the SAML
		 * assertion we can safely login into our application. We should check
		 * that this procedure is in line with the Shibboleth browser profile.
		 */
		if (null != paramUsername) {
			LOG.debug("doing a login via the SafeOnline username token");
			session.setAttribute("username", paramUsername);
		}

		chain.doFilter(request, response);
	}

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
	}
}
