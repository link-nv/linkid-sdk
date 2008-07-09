/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.SafeOnlineException;
import net.link.safeonline.authentication.service.DeviceOperationService;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestUtil;
import net.link.safeonline.sdk.auth.saml2.DeviceOperationType;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeviceOperationUtils {

	private static final Log LOG = LogFactory
			.getLog(DeviceOperationUtils.class);

	public static final String DEVICE_SERVICE_URL_INIT_PARAM = "DeviceServiceUrl";

	public static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/device/sdk/saml2/binding/saml2-post-binding.vm";

	public static final String SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM = "Saml2BrowserPostTemplate";

	private DeviceOperationUtils() {
		// empty
	}

	/**
	 * Redirects the user webapp to the external device landing page using the
	 * SAML browser post protocol. The SAML authentication request is generated
	 * by the device operation service associated with this HTPP session.
	 * <p>
	 * The method requires the <code>DeviceServiceUrl</code> init parameter
	 * defined in web.xml pointing to the location of the user web application
	 * location the device issuer should return to.
	 * </p>
	 * <p>
	 * An optional initialization parameter
	 * <code>Saml2BrowserPostTemplate</code> can be defnied in web.xml,
	 * specifying a custom SAML2 browser post velocity template.
	 * </p>
	 * 
	 * 
	 * @param landingUrl
	 *            the location at the remote device issuer where to post the
	 *            authentication request to
	 * @param device
	 * @param userId
	 *            the OLAS user ID.
	 */
	public static String redirect(String landingUrl,
            DeviceOperationType deviceOperation,
			String device, String userId) {
		LOG.debug("redirecting to: " + landingUrl);
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpSession httpSession = (HttpSession) externalContext
				.getSession(true);

		String serviceUrl = getInitParameter(externalContext,
				DEVICE_SERVICE_URL_INIT_PARAM);

		String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;
		if (externalContext
				.getInitParameter(SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM) != null) {
			templateResourceName = externalContext
					.getInitParameter(SAML2_BROWSER_POST_TEMPLATE_CONFIG_PARAM);
		}

		HttpServletResponse httpResponse = (HttpServletResponse) externalContext
				.getResponse();

		/*
		 * Next is required to preserve the session if the browser does not
		 * support cookies.
		 */
		String encodedLandingUrl = httpResponse.encodeRedirectURL(landingUrl);
		LOG.debug("landing url: " + encodedLandingUrl);

		DeviceOperationService deviceOperationService = (DeviceOperationService) httpSession
				.getAttribute(DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE);
		if (null != deviceOperationService) {
			deviceOperationService.abort();
		}
		deviceOperationService = EjbUtils.getEJB(
				"SafeOnline/DeviceOperationServiceBean/local",
				DeviceOperationService.class);
		httpSession.setAttribute(
				DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE,
				deviceOperationService);

		String encodedSamlRequestToken;
		try {
			encodedSamlRequestToken = deviceOperationService.redirect(
					serviceUrl, encodedLandingUrl, deviceOperation, device,
					userId);
		} catch (SafeOnlineException e) {
			throw new RuntimeException("could not initiate device operation: "
					+ e.getMessage(), e);
		}

		try {
			AuthnRequestUtil
					.sendAuthnRequest(encodedLandingUrl,
							encodedSamlRequestToken, templateResourceName,
							httpResponse);
		} catch (ServletException e) {
			throw new RuntimeException("could not initiate device operation: "
					+ e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException("could not initiate device operation: "
					+ e.getMessage(), e);
		}

		/*
		 * Signal the JavaServer Faces implementation that the HTTP response for
		 * this request has already been generated (such as an HTTP redirect),
		 * and that the request processing lifecycle should be terminated as
		 * soon as the current phase is completed.
		 */
		context.responseComplete();
		return null;
	}

	private static String getInitParameter(ExternalContext context,
			String parameterName) {
		String initParameter = context.getInitParameter(parameterName);
		if (null == initParameter)
            throw new RuntimeException("missing context-param in web.xml: "
					+ parameterName);
		return initParameter;
	}

}
