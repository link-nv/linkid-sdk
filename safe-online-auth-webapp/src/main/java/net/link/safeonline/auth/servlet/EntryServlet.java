/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.protocol.ProtocolContext;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.helpdesk.bean.HelpdeskManagerBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic entry point for the authentication web application. This servlet will
 * try to find out which authentication protocol is being used by the client web
 * browser to initiate an authentication procedure. We manage the authentication
 * entry via a bare-bone servlet since we:
 * <ul>
 * <li>need to be able to do some low-level GET or POST parameter parsing and
 * processing.</li>
 * <li>we want the entry point to be UI technology independent.</li>
 * </ul>
 * 
 * <p>
 * The following servlet init parameters are required:
 * </p>
 * <ul>
 * <li><code>StartUrl</code>: points to the relative/absolute URL to which
 * this servlet will redirect after successful authentication protocol entry.</li>
 * <li><code>FirstTimeUrl</code>: points to the relative/absolute URL to
 * which this servlet will redirect after first visit and successful
 * authentication protocol entry.</li>
 * <li><code>UnsupportedProtocolUrl</code>: will be used to redirect to when
 * an unsupported authentication protocol is encountered.</li>
 * <li><code>ProtocolErrorUrl</code>: will be used to redirect to when an
 * authentication protocol error is encountered.</li>
 * </ul>
 * 
 * @author fcorneli
 * 
 */
public class EntryServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(EntryServlet.class);

	private static final long serialVersionUID = 1L;

	public static final String START_URL_INIT_PARAM = "StartUrl";

	public static final String FIRST_TIME_URL_INIT_PARAM = "FirstTimeUrl";

	public static final String UNSUPPORTED_PROTOCOL_URL_INIT_PARAM = "UnsupportedProtocolUrl";

	public static final String PROTOCOL_ERROR_URL = "ProtocolErrorUrl";

	public static final String PROTOCOL_ERROR_MESSAGE_ATTRIBUTE = "protocolErrorMessage";

	public static final String PROTOCOL_NAME_ATTRIBUTE = "protocolName";

	private String startUrl;

	private String firstTimeUrl;

	private String unsupportedProtocolUrl;

	private String protocolErrorUrl;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.startUrl = getInitParameter(config, START_URL_INIT_PARAM);
		this.firstTimeUrl = getInitParameter(config, FIRST_TIME_URL_INIT_PARAM);
		this.unsupportedProtocolUrl = getInitParameter(config,
				UNSUPPORTED_PROTOCOL_URL_INIT_PARAM);
		this.protocolErrorUrl = getInitParameter(config, PROTOCOL_ERROR_URL);
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
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("GET entry");
		handleInvocation(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("POST entry");
		handleInvocation(request, response);
	}

	private void handleInvocation(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		ProtocolContext protocolContext;
		try {
			protocolContext = ProtocolHandlerManager.handleRequest(request);
		} catch (ProtocolException e) {
			redirectToProtocolErrorPage(request, response, e);
			return;
		}

		if (null == protocolContext) {
			response.sendRedirect(this.unsupportedProtocolUrl);
			return;
		}

		HttpSession session = request.getSession();
		session.setAttribute("applicationId", protocolContext
				.getApplicationId());
		session.setAttribute("target", protocolContext.getTarget());
		
		/*
		 * create new helpdesk volatile context
		 */
		HelpdeskManagerBean.clear(session);

		if (isFirstTime(request, response)) {
			response.sendRedirect(this.firstTimeUrl);
		} else {
			response.sendRedirect(this.startUrl);
		}
	}

	private void redirectToProtocolErrorPage(HttpServletRequest request,
			HttpServletResponse response, ProtocolException e)
			throws IOException {
		HttpSession session = request.getSession();
		String protocolName = e.getProtocolName();
		session.setAttribute(PROTOCOL_NAME_ATTRIBUTE, protocolName);
		String protocolErrorMessage = e.getMessage();
		session.setAttribute(PROTOCOL_ERROR_MESSAGE_ATTRIBUTE,
				protocolErrorMessage);
		response.sendRedirect(this.protocolErrorUrl);
	}

	private boolean isFirstTime(HttpServletRequest request,
			HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (null == cookies) {
			setDefloweredCookie(response);
			return true;
		}
		Cookie defloweredCookie = findDefloweredCookie(cookies);
		if (null == defloweredCookie) {
			setDefloweredCookie(response);
			return true;
		} else {
			return false;
		}
	}

	private final static String DEFLOWER_COOKIE_NAME = "deflowered";

	private Cookie findDefloweredCookie(Cookie[] cookies) {
		for (Cookie cookie : cookies) {
			if (DEFLOWER_COOKIE_NAME.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}

	private void setDefloweredCookie(HttpServletResponse response) {
		Cookie defloweredCookie = new Cookie(DEFLOWER_COOKIE_NAME, "true");
		defloweredCookie.setMaxAge(60 * 60 * 24 * 30 * 6);
		response.addCookie(defloweredCookie);
	}
}
