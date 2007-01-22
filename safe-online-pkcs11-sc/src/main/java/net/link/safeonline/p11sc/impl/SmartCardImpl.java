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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

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
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class SmartCardImpl implements SmartCard, IdentityDataCollector {

	private static Log LOG = LogFactory.getLog(SmartCardImpl.class);

	private List<SmartCardConfig> smartCardConfigs;

	private Provider pkcs11Provider;

	private SmartCardPinCallback smartCardPinCallback;

	private X509Certificate authenticationCertificate;

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

	public void open() {
		SupportedCard supportedCard = getSupportedCard();
		if (null == supportedCard) {
			throw new RuntimeException(
					"no card terminal with supported smart card inserted present");
		}
		String name = supportedCard.cardTerminal.getName();
		LOG.debug("opening PKCS11 slot of card terminal name: " + name);

		String osName = System.getProperty("os.name");
		LOG.debug("os name: " + osName);
		List<File> driverLocations = supportedCard.smartCardConfig
				.getPkcs11DriverLocations(osName);

		IdentityDataExtractor identityDataExtractor = null;
		String identityExtractorClassname = supportedCard.smartCardConfig
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
			identityDataExtractor.prePkcs11(supportedCard.cardTerminal);
		}

		File existingDriverLocation = null;
		for (File driverLocation : driverLocations) {
			if (driverLocation.exists()) {
				existingDriverLocation = driverLocation;
				break;
			}
		}
		if (null == existingDriverLocation) {
			throw new RuntimeException("no PKCS#11 driver found");
		}

		long slotIdx;
		try {
			slotIdx = getSlotIndex(existingDriverLocation, name);
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage());
		} catch (PKCS11Exception e) {
			throw new RuntimeException("PKCS11 error: " + e.getMessage());
		}

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
			loadCertificates(supportedCard);
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

	private void loadCertificates(SupportedCard supportedCard)
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

		String authenticationKeyAlias = supportedCard.smartCardConfig
				.getAuthenticationKeyAlias();
		String signatureKeyAlias = supportedCard.smartCardConfig
				.getSignatureKeyAlias();

		this.authenticationCertificate = (X509Certificate) keyStore
				.getCertificate(authenticationKeyAlias);
		this.authenticationPrivateKey = (PrivateKey) keyStore.getKey(
				authenticationKeyAlias, null);

		this.signatureCertificate = (X509Certificate) keyStore
				.getCertificate(signatureKeyAlias);
		this.signaturePrivateKey = (PrivateKey) keyStore.getKey(
				signatureKeyAlias, null);
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
		this.pkcs11Provider = new SunPKCS11(tmpConfigFile.getAbsolutePath());
	}

	private int getSlotIndex(File driverLocation, String slotDescription)
			throws IOException, PKCS11Exception {
		CK_C_INITIALIZE_ARGS ck_c_initialize_args = new CK_C_INITIALIZE_ARGS();
		PKCS11 pkcs11 = PKCS11.getInstance(driverLocation.getAbsolutePath(),
				"C_GetFunctionList", ck_c_initialize_args, false);
		try {
			long[] slotIds = pkcs11.C_GetSlotList(true);
			for (int slotIdx = 0; slotIdx < slotIds.length; slotIdx++) {
				long slotId = slotIds[slotIdx];
				CK_SLOT_INFO slotInfo = pkcs11.C_GetSlotInfo(slotId);
				LOG.debug("slot description: "
						+ new String(slotInfo.slotDescription));
				if (new String(slotInfo.slotDescription)
						.startsWith(slotDescription)) {
					LOG.debug("matching slot found");
					/*
					 * Now this is a dirty trick: most PKCS#11 driver vendors
					 * make the slot description the same as the PC/SC card
					 * terminal name. But don't tell anyone.
					 */
					if ((slotInfo.flags & PKCS11Constants.CKF_TOKEN_PRESENT) == 0) {
						continue;
					}
					/*
					 * Be careful here, it's the slot index, not the slot id.
					 */
					return slotIdx;
				}
			}
			throw new SmartCardNotFoundException(
					"no slots with expected smart card was found");
		} finally {
			pkcs11.C_Finalize(null);
		}
	}

	public boolean isReaderPresent() {
		TerminalFactory terminalFactory = TerminalFactory.getDefault();
		try {
			CardTerminals cardTerminals = terminalFactory.terminals();
			int numberOfReaders = cardTerminals.list().size();
			LOG.debug("number of readers: " + numberOfReaders);
			/*
			 * This does not always seem to work as expected.
			 */
			return numberOfReaders != 0;
		} catch (CardException e) {
			LOG.error("card error: " + e.getMessage());
			return false;
		}
	}

	public boolean isSupportedCardPresent() {
		SupportedCard supportedCard = getSupportedCard();
		boolean result = null != supportedCard;
		return result;
	}

	private static final class SupportedCard {
		private final CardTerminal cardTerminal;

		private final SmartCardConfig smartCardConfig;

		public SupportedCard(CardTerminal cardTerminal,
				SmartCardConfig smartCardConfig) {
			this.cardTerminal = cardTerminal;
			this.smartCardConfig = smartCardConfig;
		}
	}

	private SupportedCard getSupportedCard() {
		if (null == this.smartCardConfigs) {
			throw new IllegalStateException("call init first");
		}
		TerminalFactory terminalFactory = TerminalFactory.getDefault();
		CardTerminals cardTerminals = terminalFactory.terminals();
		try {
			for (CardTerminal cardTerminal : cardTerminals.list()) {
				LOG.debug("card terminal name: " + cardTerminal.getName());
				if (cardTerminal.isCardPresent()) {
					Card card = cardTerminal.connect("*");
					try {
						ATR atr = card.getATR();
						for (SmartCardConfig smartCardConfig : smartCardConfigs) {
							if (smartCardConfig.isSupportedATR(atr)) {
								return new SupportedCard(cardTerminal,
										smartCardConfig);
							}
						}
					} finally {
						card.disconnect(true);
					}
				}
			}
		} catch (CardException e) {
			LOG.error("card error: " + e.getMessage(), e);
			throw new RuntimeException("card error: " + e.getMessage(), e);
		}
		return null;
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
					passwordCallback.setPassword(this.smartCardPinCallback
							.getPin());
				}
			}
		}
	}

	public void setSmartCardPinCallback(
			SmartCardPinCallback smartCardPinCallback) {
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
}
