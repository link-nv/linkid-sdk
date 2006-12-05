package net.link.safeonline.webapp.filter;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Set;

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
			Subject subject = loginContext.getSubject();
			Set<Principal> principals = subject.getPrincipals();
			for (Principal principal : principals) {
				LOG.debug("principal: " + principal.getName());
				/*
				 * The group "Roles" is only assigned to the principal once we
				 * enter the server-side security domain. At the client-side we
				 * only have the "client-login" security domain which does
				 * nothing more but to cache the credentials for later
				 * server-side authentication.
				 */
				if (principal instanceof Group) {
					Group group = (Group) principal;
					LOG.debug("group name: " + group.getName());
					Enumeration<? extends Principal> members = group.members();
					while (members.hasMoreElements()) {
						Principal member = members.nextElement();
						LOG.debug("group member: " + member.getName());
					}
				}
			}
			Set<Object> publicCredentials = subject.getPublicCredentials();
			for (Object publicCredential : publicCredentials) {
				LOG.debug("public credential: " + publicCredential);
			}
			Set<Object> privateCredentials = subject.getPrivateCredentials();
			for (Object privateCredential : privateCredentials) {
				LOG.debug("private credential: " + privateCredential);
			}
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
