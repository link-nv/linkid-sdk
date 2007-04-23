/*
 * SafeOnline project.
 * 
 * Copyright 2005-2006 Frank Cornelis.
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardNotFoundException;
import net.link.safeonline.p11sc.SmartCardPinCallback;
import net.link.safeonline.p11sc.spi.IdentityDataCollector;
import net.link.safeonline.p11sc.spi.IdentityDataExtractor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class SmartCardImpl implements SmartCard, IdentityDataCollector {

	private static Log LOG = LogFactory.getLog(SmartCardImpl.class);

	private List<SmartCardConfig> smartCardConfigs;

	private Provider pkcs11Provider;

	private SmartCardPinCallback smartCardPinCallback;

	private X509Certificate authenticationCertificate;

	private List<X509Certificate> authenticationCertificatePath;

	private PrivateKey authenticationPrivateKey;

	private X509Certificate signatureCertificate;

	private PrivateKey signaturePrivateKey;

	private String countryCode;

	private String givenName;

	private String surname;

	private String street;

	private String postalCode;

	private String city;

	public void close() {
		if (null == this.pkcs11Provider) {
			throw new IllegalStateException("cannot close before open");
		}
		String providerName = this.pkcs11Provider.getName();
		LOG.debug("removing security provider: " + providerName);
		Security.removeProvider(providerName);
	}

	public X509Certificate getAuthenticationCertificate() {
		return this.authenticationCertificate;
	}

	public PrivateKey getAuthenticationPrivateKey() {
		return this.authenticationPrivateKey;
	}

	public X509Certificate getSignatureCertificate() {
		return this.signatureCertificate;
	}

	public PrivateKey getSignaturePrivateKey() {
		return this.signaturePrivateKey;
	}

	public void init(List<SmartCardConfig> smartCardConfigs) {
		this.smartCardConfigs = smartCardConfigs;
	}

	public boolean isOpen() {
		return false;
	}

	private SmartCardConfig getSmartCardConfig(String smartCardAlias) {
		if (null == this.smartCardConfigs) {
			throw new IllegalStateException("call init first");
		}
		for (SmartCardConfig smartCardConfig : this.smartCardConfigs) {
			if (smartCardConfig.getCardAlias().equals(smartCardAlias)) {
				return smartCardConfig;
			}
		}
		throw new IllegalArgumentException("no config found for card: "
				+ smartCardAlias);
	}

	public void open(String smartCardAlias) throws SmartCardNotFoundException {
		SmartCardConfig smartCardConfig = getSmartCardConfig(smartCardAlias);

		String osName = System.getProperty("os.name");
		LOG.debug("os name: " + osName);
		List<File> driverLocations = smartCardConfig
				.getPkcs11DriverLocations(osName);
		LOG.debug("test debug message");

		IdentityDataExtractor identityDataExtractor = null;
		String identityExtractorClassname = smartCardConfig
				.getIdentityExtractorClassname();
		if (null != identityExtractorClassname) {
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			try {
				Class<?> identityExtractorClass = classLoader
						.loadClass(identityExtractorClassname);
				if (false == IdentityDataExtractor.class
						.isAssignableFrom(identityExtractorClass)) {
					throw new RuntimeException(
							"identity extractor class of incorrect type");
				}
				identityDataExtractor = (IdentityDataExtractor) identityExtractorClass
						.newInstance();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("identity extractor class not found");
			} catch (InstantiationException e) {
				throw new RuntimeException("instantiation error: "
						+ e.getMessage());
			} catch (IllegalAccessException e) {
				throw new RuntimeException("illegal access error: "
						+ e.getMessage());
			}
		}
		if (null != identityDataExtractor) {
			identityDataExtractor.init(this);
			identityDataExtractor.prePkcs11();
		}

		File existingDriverLocation = null;
		for (File driverLocation : driverLocations) {
			LOG.debug("checking driver: " + driverLocation.getAbsolutePath());
			if (driverLocation.exists()) {
				existingDriverLocation = driverLocation;
				break;
			}
		}
		if (null == existingDriverLocation) {
			throw new RuntimeException("no PKCS#11 driver found");
		}

		long slotIdx = getBestEffortSlotIdx(existingDriverLocation);

		try {
			loadSecurityProvider(existingDriverLocation, slotIdx);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("file not found: " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage());
		}

		if (-1 == Security.addProvider(this.pkcs11Provider)) {
			throw new RuntimeException("could not add the security provider");
		}

		try {
			loadCertificates(smartCardConfig);
		} catch (UnrecoverableKeyException e) {
			throw new RuntimeException("unrecoverable key error: "
					+ e.getMessage());
		} catch (KeyStoreException e) {
			throw new RuntimeException("key store error: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("no such algo error: " + e.getMessage());
		} catch (CertificateException e) {
			throw new RuntimeException("cert error: " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage());
		}

		if (null != identityDataExtractor) {
			identityDataExtractor.postPkcs11(this.authenticationCertificate);
		}
	}

	private void loadCertificates(SmartCardConfig smartCardConfig)
			throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableKeyException {
		CallbackHandler callbackHandler = new PKCS11CallbackHandler(
				this.smartCardPinCallback);
		KeyStore.CallbackHandlerProtection callbackHandlerProtection = new KeyStore.CallbackHandlerProtection(
				callbackHandler);
		KeyStore.Builder builder = KeyStore.Builder.newInstance("PKCS11",
				this.pkcs11Provider, callbackHandlerProtection);

		KeyStore keyStore = builder.getKeyStore();
		keyStore.load(null, null);

		String authenticationKeyAlias = smartCardConfig
				.getAuthenticationKeyAlias();
		String signatureKeyAlias = smartCardConfig.getSignatureKeyAlias();

		this.authenticationCertificate = (X509Certificate) keyStore
				.getCertificate(authenticationKeyAlias);
		this.authenticationPrivateKey = (PrivateKey) keyStore.getKey(
				authenticationKeyAlias, null);

		this.signatureCertificate = (X509Certificate) keyStore
				.getCertificate(signatureKeyAlias);
		this.signaturePrivateKey = (PrivateKey) keyStore.getKey(
				signatureKeyAlias, null);

		List<X509Certificate> certificates = new LinkedList<X509Certificate>();
		Enumeration<String> aliases = keyStore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			X509Certificate cert = (X509Certificate) keyStore
					.getCertificate(alias);
			certificates.add(cert);
		}

		this.authenticationCertificatePath = constructCertificatePath(
				this.authenticationCertificate, certificates);
	}

	private List<X509Certificate> constructCertificatePath(
			X509Certificate certificate, List<X509Certificate> certificateRepo) {
		List<X509Certificate> certificatePath = new LinkedList<X509Certificate>();
		certificatePath.add(certificate);
		X509Certificate currCert = certificate;

		while (null != currCert) {
			X509Certificate issuerCert = findIssuerCertificate(currCert,
					certificateRepo);
			if (null != issuerCert) {
				certificatePath.add(issuerCert);
				if (isSelfSignedCertificate(issuerCert)) {
					break;
				}
			}
			currCert = issuerCert;
		}

		return certificatePath;
	}

	private boolean isSelfSignedCertificate(X509Certificate certificate) {
		return certificate.getIssuerX500Principal().equals(
				certificate.getSubjectX500Principal());
	}

	private X509Certificate findIssuerCertificate(X509Certificate certificate,
			List<X509Certificate> certificateRepo) {
		for (X509Certificate repoCert : certificateRepo) {
			if (certificate.getIssuerX500Principal().equals(
					repoCert.getSubjectX500Principal())) {
				return repoCert;
			}
		}
		return null;
	}

	private void loadSecurityProvider(File existingDriverLocation, long slotIdx)
			throws IOException, FileNotFoundException {
		File tmpConfigFile = File.createTempFile("pkcs11", "conf");
		tmpConfigFile.deleteOnExit();
		PrintWriter configWriter = new PrintWriter(new FileOutputStream(
				tmpConfigFile), true);
		String pkcs11Library = existingDriverLocation.getAbsolutePath();
		String name = "SmartCard";
		configWriter.println("name=" + name);
		configWriter.println("library=" + pkcs11Library);
		configWriter.println("slotListIndex=" + slotIdx);
		configWriter.close();
		LOG.debug("Initializing via PKCS#11 driver: " + pkcs11Library);
		Provider provider = Security.getProvider("SunPKCS11-" + name);
		if (null != provider) {
			throw new RuntimeException("Smart Card provider already active");
		}

		//resetPKCS11Driver();

		try {
			this.pkcs11Provider = new SunPKCS11(tmpConfigFile.getAbsolutePath());
		} catch (ProviderException e) {
			LOG.error("provider exception: " + e.getMessage());
			Throwable cause = e.getCause();
			if (null != cause) {
				LOG.error("" + cause.getMessage());
				StackTraceElement[] stackTraceElements = cause.getStackTrace();
				for (StackTraceElement stackTraceElement : stackTraceElements) {
					LOG.error(stackTraceElement.getClassName() + "."
							+ stackTraceElement.getMethodName() + " ("
							+ stackTraceElement.getFileName() + ":"
							+ stackTraceElement.getLineNumber() + ")");
				}
			}
		}
	}

	/**
	 * Resets the PKCS11 drivers used by the SunPKCS11 security provider. This
	 * fixes the issue we have when the smart card gets removed and reinserted.
	 */
	@SuppressWarnings("unchecked")
	private void resetPKCS11Driver() {
		try {
			Field moduleMapField = PKCS11.class.getDeclaredField("moduleMap");
			moduleMapField.setAccessible(true);
			Map<String, Object> moduleMap = (Map) moduleMapField.get(null);
			LOG.debug("moduleMap size: " + moduleMap.size());
			for (Map.Entry<String, Object> entry : moduleMap.entrySet()) {
				LOG.debug("finalizing " + entry.getKey());
				PKCS11 pkcs11 = (PKCS11) entry.getValue();
				Method disconnectMethod = PKCS11.class.getDeclaredMethod(
						"disconnect", new Class[] {});
				disconnectMethod.setAccessible(true);
				disconnectMethod.invoke(pkcs11, new Object[] {});
				LOG.debug("done");
			}
			moduleMap.clear();
		} catch (SecurityException e) {
			LOG.error("security error: " + e.getMessage());
			throw new RuntimeException("security error");
		} catch (NoSuchFieldException e) {
			LOG.error("no such field error: " + e.getMessage());
			throw new RuntimeException("no such field error");
		} catch (IllegalArgumentException e) {
			LOG.error("illegal argument error: " + e.getMessage());
			throw new RuntimeException("illegal argument error");
		} catch (IllegalAccessException e) {
			LOG.error("illegal access error: " + e.getMessage());
			throw new RuntimeException("illegal access error");
		} catch (NoSuchMethodException e) {
			LOG.error("nu such method error: " + e.getMessage());
			throw new RuntimeException("no such error");
		} catch (InvocationTargetException e) {
			LOG.error("invocation target error: " + e.getMessage());
			throw new RuntimeException("invocation target error");
		}
	}

	private long getBestEffortSlotIdx(File pkcs11LibraryFile)
			throws SmartCardNotFoundException {
		String pkcs11Library = pkcs11LibraryFile.getAbsolutePath();
		try {
			Method[] methods = PKCS11.class.getMethods();
			PKCS11 pkcs11 = null;
			for (Method method : methods) {
				if (!"getInstance".equals(method.getName())) {
					continue;
				}
				if ((method.getModifiers() & Modifier.STATIC) == 0) {
					continue;
				}
				LOG.debug("getInstance method found");
				Class[] paramTypes = method.getParameterTypes();
				for (Class paramType : paramTypes) {
					LOG.debug("param: " + paramType.getName());
				}
				if (Arrays.equals(new Class[] { String.class,
						CK_C_INITIALIZE_ARGS.class, Boolean.TYPE }, paramTypes)) {
					/*
					 * Java 1.5
					 */
					try {
						pkcs11 = (PKCS11) method.invoke(null, new Object[] {
								pkcs11Library, null, false });
					} catch (Exception e) {
						LOG.error("error: " + e.getMessage(), e);
						return 0; // best effort
					}
					break;
				}
				if (Arrays.equals(new Class[] { String.class, String.class,
						CK_C_INITIALIZE_ARGS.class, Boolean.TYPE }, paramTypes)) {
					/*
					 * Java 1.6
					 */
					try {
						CK_C_INITIALIZE_ARGS ck_c_initialize_args = new CK_C_INITIALIZE_ARGS();
						pkcs11 = (PKCS11) method.invoke(null, new Object[] {
								pkcs11Library, "C_GetFunctionList",
								ck_c_initialize_args, false });
					} catch (Exception e) {
						LOG.error("error during invocation of getInstance: "
								+ e.getMessage());
						return 0; // best effort
					}
					break;
				}
			}
			if (null == pkcs11) {
				LOG.warn("no appropriate PKCS11 getInstance method found");
				return 0; // best effort
			}
			long[] slotIds = pkcs11.C_GetSlotList(true);
			LOG.debug("number of PKCS11 slots: " + slotIds.length);
			for (int currSlotIdx = 0; currSlotIdx < slotIds.length; currSlotIdx++) {
				LOG.debug("slot idx: " + currSlotIdx);
				long slotId = slotIds[currSlotIdx];
				LOG.debug("slot id: " + slotId);
				CK_SLOT_INFO slotInfo = pkcs11.C_GetSlotInfo(slotId);
				LOG.debug("slot description: "
						+ new String(slotInfo.slotDescription));
				LOG.debug("manufacturer: "
						+ new String(slotInfo.manufacturerID));
				if ((slotInfo.flags & PKCS11Constants.CKF_TOKEN_PRESENT) != 0) {
					CK_TOKEN_INFO tokenInfo = pkcs11.C_GetTokenInfo(slotId);
					LOG.debug("token label: " + new String(tokenInfo.label));
					LOG.debug("token model: " + new String(tokenInfo.model));
					LOG.debug("manufacturer Id: "
							+ new String(tokenInfo.manufacturerID));
					LOG.debug("Card found in slot Idx: " + currSlotIdx);
					return currSlotIdx;
				}
			}
			throw new SmartCardNotFoundException();
			/*
			 * Do not call pkcs11.C_Finalize(null) here since this could trigger
			 * a CKR_CRYPTOKI_NOT_INITIALIZED error when using the OpenSC PKCS11
			 * driver. The PKCS11 class is caching the PKCS11 wrappers on a
			 * per-driver-path basis. If you finalize the PKCS11 wrapper you're
			 * finished using it.
			 */
		} catch (PKCS11Exception e) {
			throw new RuntimeException("PKCS11 error: " + e.getMessage(), e);
		}
	}

	private static class PKCS11CallbackHandler implements CallbackHandler {

		private static final Log LOG = LogFactory
				.getLog(PKCS11CallbackHandler.class);

		private final SmartCardPinCallback smartCardPinCallback;

		/**
		 * Main constructor.
		 * 
		 * @param smartCardPinCallback
		 *            the optional smart card PIN call back.
		 */
		public PKCS11CallbackHandler(SmartCardPinCallback smartCardPinCallback) {
			this.smartCardPinCallback = smartCardPinCallback;
		}

		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
			LOG.debug("callback handle");
			for (Callback callback : callbacks) {
				if (callback instanceof TextOutputCallback) {
					TextOutputCallback textOutputCallback = (TextOutputCallback) callback;
					LOG
							.debug("text output: "
									+ textOutputCallback.getMessage());
				} else if (callback instanceof PasswordCallback) {
					LOG.debug("password callback");
					PasswordCallback passwordCallback = (PasswordCallback) callback;
					if (null == this.smartCardPinCallback) {
						throw new RuntimeException(
								"no smart card PIN call back was provided");
					}
					char[] pin = this.smartCardPinCallback.getPin();
					if (null == pin) {
						throw new UnsupportedCallbackException(callback,
								"User canceled PIN input");
					}
					passwordCallback.setPassword(pin);
				}
			}
		}
	}

	public void setSmartCardPinCallback(
			SmartCardPinCallback smartCardPinCallback) {
		LOG.debug("setting smart card pin callback");
		this.smartCardPinCallback = smartCardPinCallback;
	}

	public String getCountryCode() {
		return this.countryCode;
	}

	public String getGivenName() {
		return this.givenName;
	}

	public String getSurname() {
		return this.surname;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return this.city;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public String getStreet() {
		return this.street;
	}

	public static void setLog(Log log) {
		SmartCardImpl.LOG = log;
	}

	public List<X509Certificate> getAuthenticationCertificatePath() {
		return this.authenticationCertificatePath;
	}
}
