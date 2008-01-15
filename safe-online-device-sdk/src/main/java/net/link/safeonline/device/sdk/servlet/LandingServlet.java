package net.link.safeonline.device.sdk.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.exception.AuthenticationInitializationException;
import net.link.safeonline.device.sdk.saml2.Saml2Handler;
import net.link.safeonline.sdk.KeyStoreUtils;

public class LandingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Map<String, String> configParams;

	private KeyPair applicationKeyPair;

	private X509Certificate applicationCertificate;

	public static final String KEYSTORE_FILE_INIT_PARAM = "KeyStoreFile";

	public static final String KEYSTORE_RESOURCE_INIT_PARAM = "KeyStoreResource";

	public static final String KEY_STORE_PASSWORD_INIT_PARAM = "KeyStorePassword";

	public static final String KEYSTORE_TYPE_INIT_PARAM = "KeyStoreType";

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.configParams = new HashMap<String, String>();
		Enumeration<String> initParamsEnum = config.getInitParameterNames();
		while (initParamsEnum.hasMoreElements()) {
			String paramName = initParamsEnum.nextElement();
			String paramValue = config.getInitParameter(paramName);
			this.configParams.put(paramName, paramValue);
		}
		String p12KeyStoreResourceName = config
				.getInitParameter(KEYSTORE_RESOURCE_INIT_PARAM);
		String p12KeyStoreFileName = config
				.getInitParameter(KEYSTORE_FILE_INIT_PARAM);
		InputStream keyStoreInputStream = null;
		if (null != p12KeyStoreResourceName) {
			Thread currentThread = Thread.currentThread();
			ClassLoader classLoader = currentThread.getContextClassLoader();
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
			this.applicationCertificate = (X509Certificate) privateKeyEntry
					.getCertificate();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Saml2Handler handler = Saml2Handler.getSaml2Handler(request);
		handler.init(this.configParams, this.applicationCertificate,
				this.applicationKeyPair);
		try {
			handler.initAuthentication(request);
		} catch (AuthenticationInitializationException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}
	}

	private String getInitParameter(ServletConfig config, String initParamName,
			String defaultValue) {
		String initParamValue = config.getInitParameter(initParamName);
		if (null == initParamValue) {
			initParamValue = defaultValue;
		}
		return initParamValue;
	}

}
