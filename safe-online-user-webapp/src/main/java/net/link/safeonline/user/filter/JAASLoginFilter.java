package net.link.safeonline.user.filter;

import java.io.IOException;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

public class JAASLoginFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(JAASLoginFilter.class);

	private static final String SESSION_LOGIN_CONTEXT = "login-context";

	private static final String JBOSS_DOMAIN_CLIENT_LOGIN = "client-login";

	public static final String SESSION_PASSWORD = "password";

	public static final String SESSION_USERNAME = "username";

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		login((HttpServletRequest) request);
		try {
			chain.doFilter(request, response);
		} finally {
			logout(request);
		}
	}

	private void login(HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute(
				SESSION_USERNAME);
		String password = (String) request.getSession().getAttribute(
				SESSION_PASSWORD);
		if (username == null) {
			return;
		}
		if (password == null) {
			return;
		}
		UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
				password.toCharArray());
		LoginContext loginContext;
		try {
			loginContext = new LoginContext(JBOSS_DOMAIN_CLIENT_LOGIN, handler);
			loginContext.login();
			LOG.debug("login for " + username);
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
			request.setAttribute(SESSION_LOGIN_CONTEXT, null);
		} catch (LoginException e) {
			LOG.error("logout error: " + e.getMessage(), e);
		}
	}

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
	}
}
