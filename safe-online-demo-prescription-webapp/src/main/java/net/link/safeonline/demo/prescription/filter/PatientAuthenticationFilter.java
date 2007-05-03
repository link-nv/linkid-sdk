/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.filter;

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
 * Patient Authentication Filter. This filter will handle the 'username' HTTP
 * request parameter as set by the SafeOnline authentication web application. We
 * use a filter for this instead of a servlet since we need to be invoked before
 * the normal SafeOnline login filter. This because we don't want to
 * re-authenticate the care provider/pharmacist, we just want to authenticate
 * the patient within the current and already authenticated http session.
 * 
 * @author fcorneli
 * 
 */
public class PatientAuthenticationFilter implements Filter {

	private static final Log LOG = LogFactory
			.getLog(PatientAuthenticationFilter.class);

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String paramUsername = httpRequest.getParameter("username");
		if (null == paramUsername) {
			chain.doFilter(request, response);
			return;
		}

		HttpSession session = httpRequest.getSession();
		LOG.debug("setting patient to: " + paramUsername);
		session.setAttribute("patient", paramUsername);

		/*
		 * We also need to filter out the 'username' HTTP request parameter,
		 * else the normal login filter will re-authenticate the current user.
		 */
		ParameterFilterHttpServletRequestWrapper requestWrapper = new ParameterFilterHttpServletRequestWrapper(
				httpRequest, "username");
		chain.doFilter(requestWrapper, response);
	}

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
	}
}
