/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.service.AuthorizationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimplePrincipal;

/**
 * Servlet Container login filter. This filter provides perceived servlet
 * container security. This means that the servlet web application that is
 * applying this filter will see meaningfull values for the
 * request.getUserPrincipal and request.isUserInRole methods. This filter does
 * not provide web resource protection itself.
 * 
 * @see <a href="http://securityfilter.sourceforge.net/test">SecurityFilter</a>
 * 
 * @author fcorneli
 * 
 */
public class ServletLoginFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(ServletLoginFilter.class);

	private AuthorizationService authorizationService;

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		HttpSession session = httpServletRequest.getSession();
		String username = (String) session.getAttribute("username");
		if (null == username) {
			chain.doFilter(request, response);
			return;
		}

		// TODO: cache roles in http request context
		Set<String> roles = this.authorizationService.getRoles(username);

		Principal userPrincipal = new SimplePrincipal(username);
		LoginHttpServletRequestWrapper loginHttpServletRequestWrapper = new LoginHttpServletRequestWrapper(
				httpServletRequest, userPrincipal, roles);

		chain.doFilter(loginHttpServletRequestWrapper, response);
	}

	public void init(@SuppressWarnings("unused")
	FilterConfig config) throws ServletException {
		LOG.debug("init");
		try {
			this.authorizationService = EjbUtils.getEJB(
					"SafeOnline/AuthorizationServiceBean/local",
					AuthorizationService.class);
		} catch (RuntimeException e) {
			throw new UnavailableException(
					"authorization service lookup failure");
		}
	}
}
