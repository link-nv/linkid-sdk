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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
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

	private SubscriptionService subscriptionService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		loadDependencies();
	}

	private void loadDependencies() {
		this.identityService = EjbUtils.getEJB(
				"SafeOnline/IdentityServiceBean/local", IdentityService.class);
		this.subscriptionService = EjbUtils.getEJB(
				"SafeOnline/SubscriptionServiceBean/local",
				SubscriptionService.class);
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

		boolean subscriptionRequired;
		try {
			subscriptionRequired = !this.subscriptionService
					.isSubscribed(applicationId);
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found: " + applicationId);
			throw new ServletException("application not found");
		}
		if (true == subscriptionRequired) {
			redirectToSubscription(response);
			return;
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

		try {
			commitAuthentication(session, applicationId);
		} catch (SubscriptionNotFoundException e) {
			throw new ServletException("subscription not found");
		} catch (ApplicationNotFoundException e) {
			throw new ServletException("application not found");
		} catch (ApplicationIdentityNotFoundException e) {
			throw new ServletException("application identity not found");
		} catch (IdentityConfirmationRequiredException e) {
			throw new ServletException("identity confirmation required");
		} catch (MissingAttributeException e) {
			throw new ServletException("missing identity attribute");
		}

		redirectToExitServlet(request, response);
	}

	private void commitAuthentication(HttpSession session, String applicationId)
			throws SubscriptionNotFoundException, ApplicationNotFoundException,
			ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException, MissingAttributeException {
		AuthenticationServiceManager.commitAuthentication(session,
				applicationId);
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

	private void redirectToSubscription(HttpServletResponse response)
			throws IOException {
		String redirectUrl = "./subscription.seam";
		response.sendRedirect(redirectUrl);
	}

	private void redirectToExitServlet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("./exit");
	}
}
