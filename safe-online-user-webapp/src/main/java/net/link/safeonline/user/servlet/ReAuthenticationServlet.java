/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.service.ReAuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This HTTP servlet initializes a stateful ReAuthentication service bean used
 * by the user web application.
 * 
 * @author wvdhaute
 * 
 */
public class ReAuthenticationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String RE_AUTH_SERVICE_ATTRIBUTE = "reAuthenticationService";

	public static final String LOGIN_URL_INIT_PARAM = "LoginUrl";

	private String loginUrl;

	private static final Log LOG = LogFactory
			.getLog(ReAuthenticationServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.loginUrl = getInitParameter(config, LOGIN_URL_INIT_PARAM);
	}

	public String getInitParameter(ServletConfig config,
			String initParameterName) throws UnavailableException {
		String paramValue = config.getInitParameter(initParameterName);
		if (null == paramValue) {
			throw new UnavailableException("missing init parameter: "
					+ initParameterName);
		}
		return paramValue;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOG.debug("GET entry");
		handleInvocation(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOG.debug("POST entry");
		handleInvocation(request, response);
	}

	private void handleInvocation(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		/*
		 * When we get an invocation we abort a possible re-auth service already
		 * on the session, then create a new re-auth service and store it on the
		 * session
		 */
		HttpSession session = request.getSession();
		ReAuthenticationService reAuthenticationService = (ReAuthenticationService) session
				.getAttribute(RE_AUTH_SERVICE_ATTRIBUTE);
		if (null != reAuthenticationService) {
			/*
			 * Something unusual has happened. Do a nice cleanup and create a
			 * new one.
			 */
			LOG.debug("aborting re-authentication service instance");
			reAuthenticationService.abort();
		}
		LOG.debug("initializing re-authentication service instance");
		reAuthenticationService = EjbUtils.getEJB(
				"SafeOnline/ReAuthenticationServiceBean/local",
				ReAuthenticationService.class);
		session
				.setAttribute(RE_AUTH_SERVICE_ATTRIBUTE,
						reAuthenticationService);
		response.sendRedirect(this.loginUrl);
	}

	/**
	 * Gives back the re-authentication service instance associated with the
	 * given HTTP session. Later on we could limit the usage of this method to
	 * certain states on the re-authentication service.
	 * 
	 * @param session
	 * @return
	 */
	public static ReAuthenticationService getReAuthenticationService(
			HttpSession session) {
		ReAuthenticationService reAuthenticationService = (ReAuthenticationService) session
				.getAttribute(RE_AUTH_SERVICE_ATTRIBUTE);
		if (null == reAuthenticationService) {
			throw new IllegalStateException(
					"re-authentication service instance not present");
		}
		return reAuthenticationService;
	}

	/**
	 * Aborts the re-authentication process.
	 * 
	 * @param session
	 */
	public static void abort(HttpSession session) {
		ReAuthenticationService reAuthenticationService = getReAuthenticationService(session);
		try {
			reAuthenticationService.abort();
		} finally {
			session.removeAttribute(RE_AUTH_SERVICE_ATTRIBUTE);
		}
	}
}
