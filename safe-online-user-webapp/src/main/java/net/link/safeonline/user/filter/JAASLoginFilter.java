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

	public static final String LOGIN_CONTEXT_PARAM = "LoginContextName";

	private static final String DEFAULT_LOGIN_CONTEXT = "client-login";

	public static final String SESSION_USERNAME_PARAM = "SessionUsernameAttributeName";

	public static final String DEFAULT_SESSION_USERNAME = "username";

	public static final String SESSION_PASSWORD_PARAM = "SessionPasswordAttributeName";

	public static final String DEFAULT_SESSION_PASSWORD = "password";

	private String loginContextName;

	private String sessionUsernameAttribute;

	private String sessionPasswordAttribute;

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.loginContextName = getInitParameter(config, LOGIN_CONTEXT_PARAM,
				DEFAULT_LOGIN_CONTEXT);
		this.sessionUsernameAttribute = getInitParameter(config,
				SESSION_USERNAME_PARAM, DEFAULT_SESSION_USERNAME);
		this.sessionPasswordAttribute = getInitParameter(config,
				SESSION_PASSWORD_PARAM, DEFAULT_SESSION_PASSWORD);
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
		login((HttpServletRequest) request);
		try {
			chain.doFilter(request, response);
		} finally {
			logout(request);
		}
	}

	private void login(HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute(
				this.sessionUsernameAttribute);
		String password = (String) request.getSession().getAttribute(
				this.sessionPasswordAttribute);
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
			loginContext = new LoginContext(this.loginContextName, handler);
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
		} catch (LoginException e) {
			LOG.error("logout error: " + e.getMessage(), e);
		}
	}
}
