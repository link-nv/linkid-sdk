/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline Authentication Filter. This filter can be used by servlet
 * container based web applications for authentication via SafeOnline. This
 * filter initiates the authentication request towards the SafeOnline
 * authentication web application. The handling of the authentication response
 * is done via the {@link LoginFilter}.
 * 
 * <p>
 * The configuration of this filter should be managed via the
 * <code>web.xml</code> deployment descriptor.
 * </p>
 * 
 * <p>
 * The init parameter <code>SafeOnlineAuthenticationServiceUrl</code> should
 * point to the SafeOnline Authentication Web Application entry point.
 * </p>
 * 
 * <p>
 * The init parameter <code>ApplicationName</code> should contain the
 * application name of this service provider.
 * </p>
 * 
 * <p>
 * The optional init parameter <code>AuthenticationProtocol</code> should
 * contain the name of the protocol used between the SafeOnline authentication
 * web application and this service provider. This can be: SIMPLE_PLAIN_URL or
 * SAML2_BROWSER_POST. Defaults to: SIMPLE_PLAIN_URL
 * </p>
 * 
 * <p>
 * The optional keystore resource name <code>KeyStoreResource</code> init
 * parameter. The key pair within this keystore can be used by the
 * authentication protocol handler to digitally sign the authentication request.
 * </p>
 * 
 * <p>
 * The optional keystore file name <code>KeyStoreFile</code> init parameter.
 * The key pair within this keystore can be used by the authentication protocol
 * handler to digitally sign the authentication request.
 * </p>
 * 
 * <p>
 * The optional <code>KeyStoreType</code> key store type init parameter.
 * Accepted values are: <code>pkcs12</code> and <code>jks</code>.
 * </p>
 * 
 * <p>
 * The optional <code>KeyStorePassword</code> init parameter contains the
 * password to unlock the keystore and key entry.
 * </p>
 * 
 * @author fcorneli
 * @see LoginFilter
 */
public class AuthenticationFilter implements Filter {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationFilter.class);

	public static final String AUTH_SERVICE_URL_INIT_PARAM = "SafeOnlineAuthenticationServiceUrl";

	public static final String APPLICATION_NAME_INIT_PARAM = "ApplicationName";

	public static final String AUTHN_PROTOCOL_INIT_PARAM = "AuthenticationProtocol";

	public static final String KEYSTORE_FILE_INIT_PARAM = "KeyStoreFile";

	public static final String KEYSTORE_RESOURCE_INIT_PARAM = "KeyStoreResource";

	public static final String KEY_STORE_PASSWORD_INIT_PARAM = "KeyStorePassword";

	public static final String KEYSTORE_TYPE_INIT_PARAM = "KeyStoreType";

	public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL = AuthenticationProtocol.SIMPLE_PLAIN_URL;

	private String safeOnlineAuthenticationServiceUrl;

	private String applicationName;

	private AuthenticationProtocol authenticationProtocol;

	private KeyPair applicationKeyPair;

	private Map<String, String> configParams;

	@SuppressWarnings("unchecked")
	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.safeOnlineAuthenticationServiceUrl = getInitParameter(config,
				AUTH_SERVICE_URL_INIT_PARAM);
		this.applicationName = getInitParameter(config,
				APPLICATION_NAME_INIT_PARAM);
		String authenticationProtocolString = getInitParameter(config,
				AUTHN_PROTOCOL_INIT_PARAM, DEFAULT_AUTHN_PROTOCOL.name());
		this.authenticationProtocol = AuthenticationProtocol
				.toAuthenticationProtocol(authenticationProtocolString);
		LOG.debug("authentication protocol: " + this.authenticationProtocol);
		String p12KeyStoreResourceName = config
				.getInitParameter(KEYSTORE_RESOURCE_INIT_PARAM);
		String p12KeyStoreFileName = config
				.getInitParameter(KEYSTORE_FILE_INIT_PARAM);
		InputStream keyStoreInputStream = null;
		if (null != p12KeyStoreResourceName) {
			Thread currentThread = Thread.currentThread();
			ClassLoader classLoader = currentThread.getContextClassLoader();
			LOG.debug("classloader name: " + classLoader.getClass().getName());
			keyStoreInputStream = classLoader
					.getResourceAsStream(p12KeyStoreResourceName);
			if (null == keyStoreInputStream) {
				throw new UnavailableException(
						"PKCS12 keystore resource not found: "
								+ p12KeyStoreResourceName);
			}
		} else if (null != p12KeyStoreFileName) {
			try {
				keyStoreInputStream = new FileInputStream(p12KeyStoreFileName);
			} catch (FileNotFoundException e) {
				throw new UnavailableException(
						"PKCS12 keystore resource not found: "
								+ p12KeyStoreFileName);
			}
		}
		if (null != keyStoreInputStream) {
			String keyStorePassword = config
					.getInitParameter(KEY_STORE_PASSWORD_INIT_PARAM);
			String keyStoreType = getInitParameter(config,
					KEYSTORE_TYPE_INIT_PARAM, "pkcs12");
			PrivateKeyEntry privateKeyEntry = KeyStoreUtils
					.loadPrivateKeyEntry(keyStoreType, keyStoreInputStream,
							keyStorePassword, keyStorePassword);
			this.applicationKeyPair = new KeyPair(privateKeyEntry
					.getCertificate().getPublicKey(), privateKeyEntry
					.getPrivateKey());
		}

		this.configParams = new HashMap<String, String>();
		Enumeration<String> initParamsEnum = config.getInitParameterNames();
		while (initParamsEnum.hasMoreElements()) {
			String paramName = initParamsEnum.nextElement();
			String paramValue = config.getInitParameter(paramName);
			this.configParams.put(paramName, paramValue);
		}
	}

	private String getInitParameter(FilterConfig config, String initParamName)
			throws UnavailableException {
		String initParam = config.getInitParameter(initParamName);
		if (null == initParam) {
			String msg = "init param \"" + initParamName
					+ "\"should be specified in web.xml";
			LOG.error(msg);
			throw new UnavailableException(msg);
		}
		return initParam;
	}

	private String getInitParameter(FilterConfig config, String initParamName,
			String defaultValue) {
		String initParamValue = config.getInitParameter(initParamName);
		if (null == initParamValue) {
			initParamValue = defaultValue;
		}
		return initParamValue;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		boolean loggedIn = LoginManager.isAuthenticated(httpRequest);
		if (false == loggedIn) {
			initiateAuthentication(httpRequest, httpResponse);
		} else {
			chain.doFilter(httpRequest, httpResponse);
		}
	}

	private void initiateAuthentication(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {
		AuthenticationProtocolHandler authenticationProtocolHandler = AuthenticationProtocolManager
				.createAuthenticationProtocolHandler(
						this.authenticationProtocol,
						this.safeOnlineAuthenticationServiceUrl,
						this.applicationName, this.applicationKeyPair,
						this.configParams, httpRequest);
		String targetUrl = httpRequest.getRequestURL().toString();
		authenticationProtocolHandler.initiateAuthentication(httpRequest,
				httpResponse, targetUrl);
	}

	public void destroy() {
		LOG.debug("destroy");
	}
}
