/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.trust;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline SSL trust manager. This class should be used by SafeOnline client
 * components to setup the SSL.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineTrustManager implements X509TrustManager {

	private static final Log LOG = LogFactory
			.getLog(SafeOnlineTrustManager.class);

	private SafeOnlineTrustManager() {
		// empty
	}

	private static SSLSocketFactory socketFactory;

	private static X509Certificate trustedCertificate;

	/**
	 * Configure the SSL for usage with SafeOnline.
	 */
	public static void configureSsl() {
		if (null == socketFactory) {
			initSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
		} else {
			if (false == socketFactory.equals(HttpsURLConnection
					.getDefaultSSLSocketFactory())) {
				throw new RuntimeException("wrong SSL socket factory installed");
			}
		}
	}

	/**
	 * Sets the trusted SafeOnline server certicate to be used by this trust
	 * manager during SSL handshake for expressing trust towards the service.
	 * 
	 * @param trustedCertificate
	 */
	public static void setTrustedCertificate(X509Certificate trustedCertificate) {
		SafeOnlineTrustManager.trustedCertificate = trustedCertificate;
	}

	private static void initSocketFactory() {
		LOG.debug("init socket factory");
		SafeOnlineTrustManager trustManagerInstance = new SafeOnlineTrustManager();
		TrustManager[] trustAllCerts = { trustManagerInstance };
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			SecureRandom secureRandom = new SecureRandom();
			sslContext.init(null, trustAllCerts, secureRandom);
			LOG.debug("SSL context provider: "
					+ sslContext.getProvider().getName());
			socketFactory = sslContext.getSocketFactory();
		} catch (KeyManagementException e) {
			String msg = "key management error: " + e.getMessage();
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		} catch (NoSuchAlgorithmException e) {
			String msg = "TLS algo not present: " + e.getMessage();
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(X509Certificate[] certs, String authType)
			throws CertificateException {
		throw new CertificateException(
				"this trust manager cannot be used as server-side trust manager");
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType)
			throws CertificateException {
		X509Certificate serverCertificate = certs[0];
		LOG.debug("server X509 subject: "
				+ serverCertificate.getSubjectX500Principal().toString());
		LOG.debug("authentication type: " + authType);
		if (null == SafeOnlineTrustManager.trustedCertificate) {
			return;
		}
		if (false == SafeOnlineTrustManager.trustedCertificate
				.equals(serverCertificate)) {
			throw new CertificateException();
		}
	}
}
