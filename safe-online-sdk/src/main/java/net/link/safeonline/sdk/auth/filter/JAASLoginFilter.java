/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 * JAAS login servlet filter. This servlet filter takes a username from the HTTP
 * session and uses it to perform a JAAS login. It also takes care of proper
 * JAAS logout.
 * 
 * When running within a JBoss Application Server, EJB components can set a
 * session attribute called {@link #FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME}
 * to flush the security domain credentials of the caller principal. The value
 * of this attribute is the name of the security domain for which to flush the
 * credential cache. This can be useful for EJB components that make changes to
 * the credentials of the caller principal.
 * 
 * @author fcorneli
 * 
 */
public class JAASLoginFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(JAASLoginFilter.class);

	private static final String SESSION_LOGIN_CONTEXT = "login-context";

	public static final String LOGIN_CONTEXT_PARAM = "LoginContextName";

	private static final String DEFAULT_LOGIN_CONTEXT = "client-login";

	public static final String SESSION_USERNAME_PARAM = "SessionUsernameAttributeName";

	public static final String DEFAULT_SESSION_USERNAME = "username";

	private String loginContextName;

	private String sessionUsernameAttribute;

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.loginContextName = getInitParameter(config, LOGIN_CONTEXT_PARAM,
				DEFAULT_LOGIN_CONTEXT);
		this.sessionUsernameAttribute = getInitParameter(config,
				SESSION_USERNAME_PARAM, DEFAULT_SESSION_USERNAME);
		LOG.debug("login context: " + this.loginContextName);
	}

	private String getInitParameter(FilterConfig config, String param,
			String defaultValue) {
		String value = config.getInitParameter(param);
		if (null == value) {
			value = defaultValue;
		}
		return value;
	}

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		login(httpServletRequest);
		try {
			chain.doFilter(request, response);
		} finally {
			logout(request);
			processFlushJBossCredentialCache(httpServletRequest);
		}
	}

	public static final String FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME = "FlushJBossCredentialCache";

	private void processFlushJBossCredentialCache(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		/*
		 * We could trigger here an java.lang.IllegalStateException: Cannot
		 * create a session after the response has been committed.
		 * 
		 * So be careful when retrieving the session.
		 */
		if (null == session) {
			return;
		}
		String securityDomain = (String) session
				.getAttribute(FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME);
		/*
		 * The EJB components can set this attribute via JACC.
		 */
		if (null == securityDomain) {
			return;
		}
		String username = (String) session
				.getAttribute(this.sessionUsernameAttribute);
		LOG.debug("trying to flush JBoss credential cache for " + username
				+ " on security domain " + securityDomain);
		try {
			SecurityManagerUtils.flushCredentialCache(username, securityDomain);
		} finally {
			session
					.removeAttribute(FLUSH_JBOSS_CREDENTIAL_CACHE_ATTRIBUTE_NAME);
		}
	}

	private void login(HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute(
				this.sessionUsernameAttribute);
		if (username == null) {
			return;
		}
		UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
				null);
		LoginContext loginContext;
		try {
			loginContext = new LoginContext(this.loginContextName, handler);
			LOG.debug("login to " + this.loginContextName + " with " + username
					+ " for " + request.getRequestURL());
			loginContext.login();
			Subject subject = loginContext.getSubject();
			for (Principal principal : subject.getPrincipals()) {
				LOG.debug("subject principal: " + principal);
			}
			LOG.debug("after login");
			request.setAttribute(SESSION_LOGIN_CONTEXT, loginContext);
		} catch (LoginException e) {
			LOG.error("login error: " + e.getMessage(), e);
		}
	}

	private void logout(ServletRequest request) {
		LoginContext loginContext = (LoginContext) request
				.getAttribute(SESSION_LOGIN_CONTEXT);
		if (loginContext == null) {
			return;
		}
		try {
			LOG.debug("logout");
			loginContext.logout();
		} catch (LoginException e) {
			LOG.error("logout error: " + e.getMessage(), e);
		}
	}
}
