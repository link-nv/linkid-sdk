/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BeID Redirect Servlet. Used to redirect the authenticated user to the target
 * application.
 * 
 * @author fcorneli
 * 
 */
public class LogonRedirectServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(LogonRedirectServlet.class);

	public static final String USER_SESSION_ATTRIBUTE = "user";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();

		String userId = (String) session.getAttribute(USER_SESSION_ATTRIBUTE);
		if (null == userId) {
			throw new ServletException("user session attribute not set");
		}

		String target = (String) session.getAttribute("target");
		if (null == target) {
			throw new ServletException("target session attribute not set");
		}

		String redirectUrl = target + "?username="
				+ URLEncoder.encode(userId, "UTF-8");

		LOG.debug("redirecting to: " + redirectUrl);
		response.sendRedirect(redirectUrl);
	}
}
