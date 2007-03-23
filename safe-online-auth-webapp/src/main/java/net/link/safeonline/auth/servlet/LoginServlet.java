/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The login servlet. A device (username-password or BeID) confirms successful
 * login by setting the username session attribute. Then the device redirects to
 * this login servlet. This login servlet will decide which is the next step in
 * the authentication process.
 * 
 * 
 * @author fcorneli
 * 
 */
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(LoginServlet.class);

	private IdentityService identityService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		loadDependencies();
	}

	private void loadDependencies() {
		this.identityService = EjbUtils.getEJB(
				"SafeOnline/IdentityServiceBean/local", IdentityService.class);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doGet");

		HttpSession session = request.getSession();
		String applicationId = (String) session.getAttribute("applicationId");
		if (null == applicationId) {
			throw new ServletException(
					"applicationId session attribute not found");
		}

		boolean confirmationRequired;
		try {
			confirmationRequired = this.identityService
					.isConfirmationRequired(applicationId);
		} catch (SubscriptionNotFoundException e) {
			throw new ServletException("subscription not found");
		} catch (ApplicationNotFoundException e) {
			throw new ServletException("application not found");
		} catch (ApplicationIdentityNotFoundException e) {
			throw new ServletException("application identity not found");
		}
		LOG.debug("confirmation required: " + confirmationRequired);
		if (true == confirmationRequired) {
			redirectToIdentityConfirmation(response);
			return;
		}

		boolean hasMissingAttributes;
		try {
			hasMissingAttributes = this.identityService
					.hasMissingAttributes(applicationId);
		} catch (ApplicationNotFoundException e) {
			throw new ServletException("application not found");
		} catch (ApplicationIdentityNotFoundException e) {
			throw new ServletException("application identity not found");
		}
		if (true == hasMissingAttributes) {
			redirectToMissingAttributes(response);
			return;
		}

		redirectToApplication(request, response);
	}

	private void redirectToMissingAttributes(HttpServletResponse response)
			throws IOException {
		String redirectUrl = "./missing-attributes.seam";
		response.sendRedirect(redirectUrl);
	}

	private void redirectToIdentityConfirmation(HttpServletResponse response)
			throws ServletException, IOException {
		String redirectUrl = "./identity-confirmation.seam";

		LOG.debug("redirecting to: " + redirectUrl);
		response.sendRedirect(redirectUrl);
	}

	private void redirectToApplication(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();

		String userId = (String) session.getAttribute("username");
		if (null == userId) {
			throw new ServletException("user session attribute not set");
		}

		String target = (String) session.getAttribute("target");
		if (null == target) {
			throw new ServletException("target session attribute not set");
		}

		String redirectUrl = target + "?username="
				+ URLEncoder.encode(userId, "UTF-8");

		/*
		 * Seam.invalidateSession does not work from within a servlet.
		 */
		session.invalidate();

		LOG.debug("redirecting to: " + redirectUrl);
		response.sendRedirect(redirectUrl);
	}
}
