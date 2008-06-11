/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.seam;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.device.sdk.DeviceManager;
import net.link.safeonline.device.sdk.auth.saml2.Saml2BrowserPostHandler;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

/**
 * Utility class for usage within a JBoss Seam JSF based web application.
 * 
 * @author wvdhaute
 * 
 */
public class SafeOnlineDeviceUtils {

	public static final String DEVICE_AUTH_SERVICE_URL_INIT_PARAM = "DeviceAuthenticationServiceUrl";

	public static final String APPLICATION_NAME_INIT_PARAM = "ApplicationName";

	private SafeOnlineDeviceUtils() {
		// empty
	}

	/**
	 * Redirects to the specified Device issuer authentication page
	 * 
	 * <p>
	 * The method requires the <code>DeviceAuthenticationServiceUrl</code>
	 * context parameter defined in web.xml pointing to the location of the
	 * authentication web application location the Device issuer should return
	 * to.
	 * </p>
	 * 
	 * @param facesMessages
	 * @param log
	 * @param landingUrl
	 *            the landing page of the device issuer
	 */
	@SuppressWarnings("unchecked")
	public static String authenticate(FacesMessages facesMessages, Log log,
			String landingUrl, String device) {
		log.debug("redirecting to #0", landingUrl);

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		String authenticationServiceUrl = getInitParameter(externalContext,
				DEVICE_AUTH_SERVICE_URL_INIT_PARAM);

		String applicationName = getInitParameter(externalContext,
				APPLICATION_NAME_INIT_PARAM);

		IdentityServiceClient identityServiceClient = new IdentityServiceClient();
		KeyPair keyPair = new KeyPair(identityServiceClient.getPublicKey(),
				identityServiceClient.getPrivateKey());

		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		NodeAuthenticationService nodeAuthenticationService = EjbUtils.getEJB(
				"SafeOnline/NodeAuthenticationServiceBean/local",
				NodeAuthenticationService.class);
		String nodeName;
		try {
			nodeName = nodeAuthenticationService
					.authenticate(authIdentityServiceClient.getCertificate());
		} catch (NodeNotFoundException e) {
			throw new RuntimeException("could not initiate authentication: "
					+ e.getMessage(), e);
		}

		HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext
				.getRequest();
		HttpServletResponse httpServletResponse = (HttpServletResponse) externalContext
				.getResponse();
		/*
		 * Next is required to preserve the session if the browser does not
		 * support cookies.
		 */
		String encodedLandingUrl = httpServletResponse
				.encodeRedirectURL(landingUrl);
		log.debug("landing url: #0", encodedLandingUrl);

		Map<String, String> configParams = externalContext
				.getInitParameterMap();

		Saml2BrowserPostHandler saml2BrowserPostHandler = Saml2BrowserPostHandler
				.getSaml2BrowserPostHandler(httpServletRequest);
		saml2BrowserPostHandler.init(authenticationServiceUrl, nodeName,
				applicationName, keyPair, configParams);

		try {
			saml2BrowserPostHandler.authnRequest(httpServletRequest,
					httpServletResponse, encodedLandingUrl, device);
		} catch (Exception e) {
			throw new RuntimeException("could not initiate authentication: "
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

	/**
	 * Redirects from Device issuer to OLAS upon completion of the device
	 * registration/removal.
	 * 
	 * @throws IOException
	 */
	public static void deviceExit() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		externalContext
				.redirect(DeviceManager
						.getSafeOnlineDeviceExitServiceUrl((HttpSession) externalContext
								.getSession(true)));
	}
}
