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
import java.security.cert.X509Certificate;

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
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.util.servlet.AbstractInjectionFilter;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline Authentication Request Filter. This filter can be used by servlet
 * container based web applications for authentication via SafeOnline. This
 * filter initiates the authentication request towards the SafeOnline
 * authentication web application. The handling of the authentication response
 * is done via the {@link AuthnResponseFilter}.
 * 
 * <p>
 * The configuration of this filter should be managed via the
 * <code>web.xml</code> deployment descriptor.
 * </p>
 * 
 * <p>
 * The init parameter <code>AuthenticationServiceUrl</code> should point to
 * the Authentication Web Application entry point.
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
 * web application and this service provider. This can be: SAML2_BROWSER_POST.
 * Defaults to: SAML2_BROWSER_POST
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
 * @see AuthnResponseFilter
 */
public class AuthnRequestFilter extends AbstractInjectionFilter {

	private static final Log LOG = LogFactory.getLog(AuthnRequestFilter.class);

	public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL = AuthenticationProtocol.SAML2_BROWSER_POST;

	@Init(name = "AuthenticationServiceUrl")
	private String authenticationServiceUrl;

	@Init(name = "ApplicationName")
	private String applicationName;

	@Init(name = "ApplicationFriendlyName", optional = true)
	private String applicationFriendlyName;

	@Init(name = "AuthenticationProtocol", optional = true)
	private String authenticationProtocolString;

	private AuthenticationProtocol authenticationProtocol;

	@Init(name = "KeyStoreResource", optional = true)
	private String p12KeyStoreResourceName;

	@Init(name = "KeyStoreFile", optional = true)
	private String p12KeyStoreFileName;

	@Init(name = "KeyStorePassword")
	private String keyStorePassword;

	@Init(name = "KeyStoreType", defaultValue = "pkcs12")
	private String keyStoreType;

	private KeyPair applicationKeyPair;

	private X509Certificate applicationCertificate;

	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);
		LOG.debug("init");
		if (null == this.authenticationProtocolString) {
			this.authenticationProtocol = DEFAULT_AUTHN_PROTOCOL;
		} else {
			this.authenticationProtocol = AuthenticationProtocol
					.toAuthenticationProtocol(this.authenticationProtocolString);
		}
		LOG.debug("authentication protocol: " + this.authenticationProtocol);
		InputStream keyStoreInputStream = null;
		if (null != this.p12KeyStoreResourceName) {
			Thread currentThread = Thread.currentThread();
			ClassLoader classLoader = currentThread.getContextClassLoader();
			LOG.debug("classloader name: " + classLoader.getClass().getName());
			keyStoreInputStream = classLoader
					.getResourceAsStream(this.p12KeyStoreResourceName);
			if (null == keyStoreInputStream) {
				throw new UnavailableException(
						"PKCS12 keystore resource not found: "
								+ this.p12KeyStoreResourceName);
			}
		} else if (null != this.p12KeyStoreFileName) {
			try {
				keyStoreInputStream = new FileInputStream(
						this.p12KeyStoreFileName);
			} catch (FileNotFoundException e) {
				throw new UnavailableException(
						"PKCS12 keystore resource not found: "
								+ this.p12KeyStoreFileName);
			}
		}
		if (null != keyStoreInputStream) {
			PrivateKeyEntry privateKeyEntry = KeyStoreUtils
					.loadPrivateKeyEntry(this.keyStoreType,
							keyStoreInputStream, this.keyStorePassword,
							this.keyStorePassword);
			this.applicationKeyPair = new KeyPair(privateKeyEntry
					.getCertificate().getPublicKey(), privateKeyEntry
					.getPrivateKey());
			this.applicationCertificate = (X509Certificate) privateKeyEntry
					.getCertificate();
		}
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
		AuthenticationProtocolManager.createAuthenticationProtocolHandler(
				this.authenticationProtocol, this.authenticationServiceUrl,
				this.applicationName, this.applicationFriendlyName,
				this.applicationKeyPair, this.applicationCertificate,
				this.configParams, httpRequest);
		AuthenticationProtocolManager.initiateAuthentication(httpRequest,
				httpResponse);
	}

	public void destroy() {
		LOG.debug("destroy");
	}
}
