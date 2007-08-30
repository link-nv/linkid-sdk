/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.seam;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;

import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

/**
 * Utility class for usage within a JBoss Seam JSF based web application.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineLoginUtils {

	public static final String SAFE_ONLINE_AUTH_SERVICE_URL_INIT_PARAM = "SafeOnlineAuthenticationServiceUrl";

	public static final String APPLICATION_NAME_INIT_PARAM = "ApplicationName";

	public static final String AUTHN_PROTOCOL_INIT_PARAM = "AuthenticationProtocol";

	public static final String KEY_STORE_RESOURCE_INIT_PARAM = "KeyStoreResource";

	public static final String KEY_STORE_FILE_INIT_PARAM = "KeyStoreFile";

	public static final String KEY_STORE_TYPE_INIT_PARAM = "KeyStoreType";

	public static final String KEY_STORE_PASSWORD_INIT_PARAM = "KeyStorePassword";

	public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL = AuthenticationProtocol.SIMPLE_PLAIN_URL;

	private SafeOnlineLoginUtils() {
		// empty
	}

	/**
	 * Performs a SafeOnline login using the SafeOnline authentication web
	 * application.
	 * 
	 * <p>
	 * The method requires the <code>SafeOnlineAuthenticationServiceUrl</code>
	 * context parameter defined in web.xml pointing to the location of the
	 * SafeOnline authentication web application.
	 * </p>
	 * 
	 * <p>
	 * The method also requires the <code>ApplicationName</code> context
	 * parameter defined in web.xml containing the application name that will be
	 * communicated towards the SafeOnline authentication web application.
	 * </p>
	 * 
	 * @param facesMessages
	 * @param log
	 * @param targetPage
	 *            the page to which the user should be redirected after login.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String login(FacesMessages facesMessages, Log log,
			String targetPage) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		String safeOnlineAuthenticationServiceUrl = getInitParameter(
				externalContext, SAFE_ONLINE_AUTH_SERVICE_URL_INIT_PARAM);
		log.debug("redirecting to #0", safeOnlineAuthenticationServiceUrl);

		String applicationName = getInitParameter(externalContext,
				APPLICATION_NAME_INIT_PARAM);

		String authenticationProtocolString = getInitParameter(externalContext,
				AUTHN_PROTOCOL_INIT_PARAM, DEFAULT_AUTHN_PROTOCOL.name());
		AuthenticationProtocol authenticationProtocol;
		try {
			authenticationProtocol = AuthenticationProtocol
					.toAuthenticationProtocol(authenticationProtocolString);
		} catch (UnavailableException e) {
			throw new RuntimeException(
					"could not parse authentication protocol: "
							+ authenticationProtocolString);
		}
		log.debug("authentication protocol: #0", authenticationProtocol);

		String keyStoreResource = externalContext
				.getInitParameter(KEY_STORE_RESOURCE_INIT_PARAM);
		String keyStoreFile = externalContext
				.getInitParameter(KEY_STORE_FILE_INIT_PARAM);
		KeyPair keyPair;
		if (null != keyStoreResource || null != keyStoreFile) {
			if (null != keyStoreResource && null != keyStoreFile) {
				throw new RuntimeException(
						"both KeyStoreResource and KeyStoreFile are defined");
			}
			String keyStorePassword = getInitParameter(externalContext,
					KEY_STORE_PASSWORD_INIT_PARAM);
			String keyStoreType = getInitParameter(externalContext,
					KEY_STORE_TYPE_INIT_PARAM);
			InputStream keyStoreInputStream;
			if (null != keyStoreResource) {
				keyStoreInputStream = Thread.currentThread()
						.getContextClassLoader().getResourceAsStream(
								keyStoreResource);
				if (null == keyStoreInputStream) {
					throw new RuntimeException("resource not found: "
							+ keyStoreResource);
				}
			} else {
				try {
					keyStoreInputStream = new FileInputStream(keyStoreFile);
				} catch (FileNotFoundException e) {
					throw new RuntimeException("file not found: "
							+ keyStoreFile);
				}
			}
			PrivateKeyEntry privateKeyEntry = KeyStoreUtils
					.loadPrivateKeyEntry(keyStoreType, keyStoreInputStream,
							keyStorePassword, keyStorePassword);
			keyPair = new KeyPair(privateKeyEntry.getCertificate()
					.getPublicKey(), privateKeyEntry.getPrivateKey());
		} else {
			keyPair = null;
		}

		HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext
				.getRequest();
		HttpServletResponse httpServletResponse = (HttpServletResponse) externalContext
				.getResponse();
		String requestUrl = httpServletRequest.getRequestURL().toString();
		String targetUrl = getTargetUrl(requestUrl, targetPage);
		/*
		 * Next is required to preserve the session if the browser does not
		 * support cookies.
		 */
		targetUrl = httpServletResponse.encodeRedirectURL(targetUrl);
		log.debug("target url: #0", targetUrl);

		Map<String, String> configParams = externalContext
				.getInitParameterMap();

		AuthenticationProtocolHandler authenticationProtocolHandler;
		try {
			authenticationProtocolHandler = AuthenticationProtocolManager
					.createAuthenticationProtocolHandler(
							authenticationProtocol,
							safeOnlineAuthenticationServiceUrl,
							applicationName, keyPair, configParams,
							httpServletRequest);
		} catch (ServletException e) {
			throw new RuntimeException(
					"could not init authentication protocol handler: "
							+ authenticationProtocol + "; original message: "
							+ e.getMessage(), e);
		}
		try {
			authenticationProtocolHandler.initiateAuthentication(
					httpServletRequest, httpServletResponse, targetUrl);
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

	public static String getTargetUrl(String requestUrl, String targetPage) {
		int lastSlashIdx = requestUrl.lastIndexOf("/");
		String prefix = requestUrl.substring(0, lastSlashIdx);
		String targetUrl = prefix + "/" + targetPage;
		return targetUrl;
	}

	private static String getInitParameter(ExternalContext context,
			String parameterName) {
		String initParameter = context.getInitParameter(parameterName);
		if (null == initParameter) {
			throw new RuntimeException("missing context-param in web.xml: "
					+ parameterName);
		}
		return initParameter;
	}

	private static String getInitParameter(ExternalContext context,
			String parameterName, String defaultValue) {
		String initParameter = context.getInitParameter(parameterName);
		if (null == initParameter) {
			initParameter = defaultValue;
		}
		return initParameter;
	}
}
