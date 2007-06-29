/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.demo.mandate.AuthorizationService;
import net.link.safeonline.demo.mandate.AuthorizationServiceFactory;
import net.link.safeonline.demo.mandate.MandateConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(LoginServlet.class);

	private AuthorizationService authorizationService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOG.debug("init");

		this.authorizationService = AuthorizationServiceFactory.newInstance();
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		String username = (String) session.getAttribute("username");
		LOG.debug("username: " + username);

		boolean admin = this.authorizationService.isAdmin(username);

		if (admin) {
			redirectToAdminPage(session, response);
		} else {
			redirectToOverviewPage(session, response);
		}
	}

	private void redirectToOverviewPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", MandateConstants.USER_ROLE);
		/*
		 * The role attribute is used by the LawyerLoginModule for
		 * authorization.
		 */
		response.sendRedirect("./overview.seam");
	}

	private void redirectToAdminPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", MandateConstants.ADMIN_ROLE);
		response.sendRedirect("./admin/search.seam");
	}
}
