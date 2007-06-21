/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.seam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

/**
 * Utility class for usage with a JBoss Seam based web application.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineLoginUtils {

	public static final String SAFE_ONLINE_AUTH_SERVICE_URL_INIT_PARAM = "SafeOnlineAuthenticationServiceUrl";

	public static final String APPLICATION_NAME_INIT_PARAM = "ApplicationName";

	private SafeOnlineLoginUtils() {
		// empty
	}

	/**
	 * Performs a SafeOnline login using the SafeOnline authentication web
	 * application. The method requires the
	 * <code>SafeOnlineAuthenticationServiceUrl</code> context parameter
	 * defined in web.xml pointing to the location of the SafeOnline
	 * authentication web application. The method also requires the
	 * <code>ApplicationName</code> context parameter defined in web.xml
	 * containing the application name that will be communicated towards the
	 * SafeOnline authentication web application.
	 * 
	 * @param facesMessages
	 * @param log
	 * @param targetPage
	 *            the page to which the user should be redirected after login.
	 * @return
	 */
	public static String login(FacesMessages facesMessages, Log log,
			String targetPage) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		String safeOnlineAuthenticationServiceUrl = externalContext
				.getInitParameter(SAFE_ONLINE_AUTH_SERVICE_URL_INIT_PARAM);
		if (null == safeOnlineAuthenticationServiceUrl) {
			throw new RuntimeException("no "
					+ SAFE_ONLINE_AUTH_SERVICE_URL_INIT_PARAM
					+ " init parameter defined");
		}
		log.debug("redirecting to #0", safeOnlineAuthenticationServiceUrl);
		String applicationName = externalContext
				.getInitParameter(APPLICATION_NAME_INIT_PARAM);
		if (null == applicationName) {
			throw new RuntimeException("no " + APPLICATION_NAME_INIT_PARAM
					+ " init parameter defined");
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
		String redirectUrl;
		try {
			redirectUrl = safeOnlineAuthenticationServiceUrl + "?application="
					+ URLEncoder.encode(applicationName, "UTF-8") + "&target="
					+ URLEncoder.encode(targetUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "UnsupportedEncoding: " + e.getMessage();
			log.debug(msg);
			facesMessages.add(msg);
			return null;
		}
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			log.debug(msg);
			facesMessages.add(msg);
			return null;
		}
		return null;
	}

	public static String getTargetUrl(String requestUrl, String targetPage) {
		int lastSlashIdx = requestUrl.lastIndexOf("/");
		String prefix = requestUrl.substring(0, lastSlashIdx);
		String targetUrl = prefix + "/" + targetPage;
		return targetUrl;
	}
}
