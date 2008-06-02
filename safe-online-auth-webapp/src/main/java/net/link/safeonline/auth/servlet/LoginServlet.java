/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.servlet.AbstractInjectionServlet;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The login servlet. A device (username-password or BeID) confirms successful
 * login by setting the 'username' session attribute. Then the device redirects
 * to this login servlet. This login servlet will decide which is the next step
 * in the authentication process.
 * 
 * 
 * @author fcorneli
 * 
 */
public class LoginServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(LoginServlet.class);

	@EJB(mappedName = "SafeOnline/IdentityServiceBean/local")
	private IdentityService identityService;

	@EJB(mappedName = "SafeOnline/SubscriptionServiceBean/local")
	private SubscriptionService subscriptionService;

	@EJB(mappedName = "SafeOnline/DevicePolicyServiceBean/local")
	private DevicePolicyService devicePolicyService;

	@EJB(mappedName = "SafeOnline/UsageAgreementServiceBean/local")
	private UsageAgreementService usageAgreementService;

	@Override
	protected void invoke(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doGet");
		HttpSession session = request.getSession();
		String applicationId = getApplicationId(session);
		DeviceEntity device = getDevice(session);
		String username = getUsername(session);

		boolean globalConfirmationRequired = performGlobalUsageAgreementCheck();
		if (true == globalConfirmationRequired) {
			redirectToGlobalConfirmation(response);
			return;
		}
		HelpdeskLogger.add(session,
				"global usage agreement confirmation found", LogLevelType.INFO);

		boolean devicePolicyCheck = performDevicePolicyCheck(session,
				applicationId, device);
		if (false == devicePolicyCheck) {
			redirectToRegisterDevice(username, session, response);
			return;
		}
		HelpdeskLogger.add(session, "authn device OK", LogLevelType.INFO);

		boolean subscriptionRequired = performSubscriptionCheck(session,
				applicationId);
		if (true == subscriptionRequired) {
			redirectToSubscription(response);
			return;
		}
		HelpdeskLogger.add(session, "subscription found", LogLevelType.INFO);

		boolean confirmationRequired = performConfirmationCheck(applicationId);
		if (true == confirmationRequired) {
			redirectToIdentityConfirmation(response);
			return;
		}
		HelpdeskLogger.add(session, "confirmation found", LogLevelType.INFO);

		boolean hasMissingAttributes = performMissingAttributesCheck(session,
				applicationId);
		if (true == hasMissingAttributes) {
			redirectToMissingAttributes(response);
			return;
		}
		HelpdeskLogger.add(session, "necessary attributes found",
				LogLevelType.INFO);

		/*
		 * We can commit the authentication process here.
		 */
		response.sendRedirect("./exit");
	}

	private void redirectToRegisterDevice(
			@SuppressWarnings("unused") String username,
			@SuppressWarnings("unused") HttpSession session,
			HttpServletResponse response) throws IOException {
		response.sendRedirect("./register-device.seam");
	}

	private boolean performGlobalUsageAgreementCheck() {
		return this.usageAgreementService
				.requiresGlobalUsageAgreementAcceptation();
	}

	private boolean performMissingAttributesCheck(HttpSession session,
			String applicationId) throws ServletException {
		boolean hasMissingAttributes;
		try {
			hasMissingAttributes = this.identityService
					.hasMissingAttributes(applicationId);
		} catch (ApplicationNotFoundException e) {
			throw new ServletException("application not found");
		} catch (ApplicationIdentityNotFoundException e) {
			HelpdeskLogger.add(session, "application identity not found",
					LogLevelType.ERROR);
			throw new ServletException("application identity not found");
		} catch (PermissionDeniedException e) {
			throw new ServletException("permission denied: " + e.getMessage());
		} catch (AttributeTypeNotFoundException e) {
			throw new ServletException("attribute type not found");
		}
		return hasMissingAttributes;
	}

	private boolean performConfirmationCheck(String applicationId)
			throws ServletException {
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
		return confirmationRequired;
	}

	private boolean performSubscriptionCheck(HttpSession session,
			String applicationId) throws ServletException {
		boolean subscriptionRequired;
		try {
			subscriptionRequired = !this.subscriptionService
					.isSubscribed(applicationId);
			if (!subscriptionRequired)
				try {
					subscriptionRequired = this.usageAgreementService
							.requiresUsageAgreementAcceptation(applicationId);
				} catch (SubscriptionNotFoundException e) {
					LOG.debug("subscription not found: " + applicationId);
					HelpdeskLogger.add(session, "subscription not found: "
							+ applicationId, LogLevelType.ERROR);
					throw new ServletException("subscription not found");
				}
		} catch (ApplicationNotFoundException e) {
			LOG.debug("application not found: " + applicationId);
			HelpdeskLogger.add(session, "application not found: "
					+ applicationId, LogLevelType.ERROR);
			throw new ServletException("application not found");
		}
		return subscriptionRequired;
	}

	/**
	 * Check whether the used authentication device is sufficient for the given
	 * application.
	 * 
	 * @param session
	 * @param applicationId
	 * @param device
	 * @throws ServletException
	 */
	private boolean performDevicePolicyCheck(HttpSession session,
			String applicationId, DeviceEntity device) throws ServletException {
		Set<DeviceEntity> requiredDevicePolicy = LoginManager
				.getRequiredDevices(session);
		List<DeviceEntity> devicePolicy;
		try {
			devicePolicy = this.devicePolicyService.getDevicePolicy(
					applicationId, requiredDevicePolicy);
		} catch (ApplicationNotFoundException e) {
			throw new ServletException("application not found: "
					+ applicationId);
		} catch (EmptyDevicePolicyException e) {
			throw new ServletException("empty device policy");
		}
		if (devicePolicy.contains(device))
			return true;
		return false;
	}

	private String getUsername(HttpSession session) throws ServletException {
		String username = (String) session.getAttribute("username");
		if (null == username) {
			HelpdeskLogger.add(session, "username session attribute not set",
					LogLevelType.ERROR);
			throw new ServletException("username session attribute not set");
		}
		return username;
	}

	private DeviceEntity getDevice(HttpSession session) {
		DeviceEntity device = LoginManager.getAuthenticationDevice(session);
		LOG.debug("authentication device: " + device.getName());
		HelpdeskLogger.add(session, "authenticated via " + device.getName(),
				LogLevelType.INFO);
		return device;
	}

	private String getApplicationId(HttpSession session)
			throws ServletException {
		String applicationId = LoginManager.findApplication(session);
		if (null == applicationId) {
			HelpdeskLogger.add(session,
					"applicationId session attribute not found",
					LogLevelType.ERROR);
			throw new ServletException(
					"applicationId session attribute not set");
		}
		return applicationId;
	}

	private void redirectToMissingAttributes(HttpServletResponse response)
			throws IOException {
		String redirectUrl = "./missing-attributes.seam";
		response.sendRedirect(redirectUrl);
	}

	private void redirectToIdentityConfirmation(HttpServletResponse response)
			throws IOException {
		String redirectUrl = "./identity-confirmation.seam";

		LOG.debug("redirecting to: " + redirectUrl);
		response.sendRedirect(redirectUrl);
	}

	private void redirectToSubscription(HttpServletResponse response)
			throws IOException {
		String redirectUrl = "./subscription.seam";
		response.sendRedirect(redirectUrl);
	}

	private void redirectToGlobalConfirmation(HttpServletResponse response)
			throws IOException {
		String redirectUrl = "./global-confirmation.seam";
		response.sendRedirect(redirectUrl);
	}
}
