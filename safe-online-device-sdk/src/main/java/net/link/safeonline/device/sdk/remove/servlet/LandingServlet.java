package net.link.safeonline.device.sdk.remove.servlet;

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

import net.link.safeonline.device.sdk.DeviceManager;
import net.link.safeonline.device.sdk.reg.saml2.Saml2BrowserPostHandler;
import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.sdk.servlet.annotation.Context;
import net.link.safeonline.sdk.servlet.annotation.Init;

import org.jboss.seam.annotations.web.RequestParameter;

public class LandingServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	@Init(name = "RemovalServiceUrl")
	private String removalServiceUrl;

	@Context(name = "KeyStoreResource", optional = true)
	private String p12KeyStoreResourceName;

	@Context(name = "KeyStoreFile", optional = true)
	private String p12KeyStoreFileName;

	@Context(name = "KeyStorePassword", optional = true)
	private String keyStorePassword;

	@Context(name = "KeyStoreType", defaultValue = "pkcs12")
	private String keyStoreType;

	@Context(name = "ApplicationName")
	private String applicationName;

	@Context(name = "DeviceName")
	private String deviceName;

	/**
	 * The 'source' request parameter is used to find out to who the
	 * communication should be directed. This can be 'user' for the user web
	 * application or 'auth' for the authentication web application.
	 */
	@RequestParameter("source")
	private String source;

	@RequestParameter("node")
	private String node;

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
	protected void invokeGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		DeviceManager.setServiceUrls(request.getSession(), this.node,
				this.source);

		Saml2BrowserPostHandler saml2BrowserPostHandler = Saml2BrowserPostHandler
				.getSaml2BrowserPostHandler(request);
		saml2BrowserPostHandler.init(this.removalServiceUrl,
				this.applicationName, this.applicationKeyPair,
				this.applicationCertificate, this.configParams);
		String targetUrl = DeviceManager
				.getSafeOnlineDeviceLandingServiceUrl(request.getSession());

		saml2BrowserPostHandler.authnRequest(request, response, targetUrl,
				this.deviceName);
	}

}
