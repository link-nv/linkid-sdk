/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.auth.saml2.Saml2Handler;
import net.link.safeonline.device.sdk.exception.AuthenticationInitializationException;
import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.sdk.servlet.ErrorMessage;
import net.link.safeonline.sdk.servlet.annotation.Context;
import net.link.safeonline.sdk.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LandingServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(LandingServlet.class);

	@Init(name = "AuthenticationUrl")
	private String authenticationUrl;

	@Context(name = "KeyStoreResource", optional = true)
	private String p12KeyStoreResourceName;

	@Context(name = "KeyStoreFile", optional = true)
	private String p12KeyStoreFileName;

	@Context(name = "KeyStorePassword", optional = true)
	private String keyStorePassword;

	@Context(name = "KeyStoreType", defaultValue = "pkcs12")
	private String keyStoreType;

	@Init(name = "ErrorPage", optional = true)
	private String errorPage;

	private KeyPair applicationKeyPair;

	private X509Certificate applicationCertificate;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		InputStream keyStoreInputStream = null;
		if (null != this.p12KeyStoreResourceName) {
			Thread currentThread = Thread.currentThread();
			ClassLoader classLoader = currentThread.getContextClassLoader();
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

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		try {
			Saml2Handler handler = Saml2Handler.getSaml2Handler(request);
			handler.init(this.configParams, this.applicationCertificate,
					this.applicationKeyPair);
			handler.initAuthentication(request);
		} catch (AuthenticationInitializationException e) {
			redirectToErrorPage(request, response, this.errorPage, null,
					new ErrorMessage(e.getMessage()));
			return;
		}
		response.sendRedirect(this.authenticationUrl);
	}
}
