/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.p11sc.beid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import junit.framework.TestCase;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardConfigFactory;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.SmartCardPinCallback;
import net.link.safeonline.p11sc.impl.SmartCardConfigFactoryImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.CK_INFO;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import be.belgium.eid.BEID_Address;
import be.belgium.eid.BEID_Certif_Check;
import be.belgium.eid.BEID_ID_Data;
import be.belgium.eid.BEID_Long;
import be.belgium.eid.BEID_Status;
import be.belgium.eid.eidlib;

public class SmartCardTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(SmartCardTest.class);

	public void testGetCertificatePath() throws Exception {
		// setup
		SmartCard smartCard = SmartCardFactory.newInstance();
		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();

		smartCard.init(configFactory.getSmartCardConfigs());

		// operate
		smartCard.open("beid");
		try {
			List<X509Certificate> resultPath = smartCard
					.getAuthenticationCertificatePath();

			// verify
			assertNotNull(resultPath);
			LOG.debug("result path size: " + resultPath.size());
			assertTrue(resultPath.size() > 1);

			for (X509Certificate resultCert : resultPath) {
				LOG.debug("cert subject: "
						+ resultCert.getSubjectX500Principal().toString());
				File tmpFile = File.createTempFile("cert-", ".crt");
				FileUtils
						.writeByteArrayToFile(tmpFile, resultCert.getEncoded());
			}
		} finally {
			smartCard.close();
		}
	}

	public void testGetCertificatesAndPrivateKeys() throws Exception {
		// setup
		SmartCard smartCard = SmartCardFactory.newInstance();
		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();

		smartCard.init(configFactory.getSmartCardConfigs());

		// operate
		smartCard.open("beid");

		try {
			// verify
			X509Certificate authCertResult = smartCard
					.getAuthenticationCertificate();
			X509Certificate signCertResult = smartCard
					.getSignatureCertificate();
			PrivateKey authPrivateKey = smartCard.getAuthenticationPrivateKey();
			PrivateKey signPrivateKey = smartCard.getSignaturePrivateKey();
			LOG.debug("authentication certificate: " + authCertResult);
			assertNotNull(authCertResult);
			assertNotNull(signCertResult);
			assertNotNull(authPrivateKey);
			assertNotNull(signPrivateKey);

			File tmpCertFile = File.createTempFile("cert-", ".crt");
			FileOutputStream outputStream = new FileOutputStream(tmpCertFile);
			outputStream.write(authCertResult.getEncoded());
			outputStream.close();

			String resultGivenName = smartCard.getGivenName();
			String resultSurname = smartCard.getSurname();
			String resultCountryCode = smartCard.getCountryCode();
			LOG.debug("given name: " + resultGivenName);
			LOG.debug("sur name: " + resultSurname);
			LOG.debug("country code: " + resultCountryCode);
			assertNotNull(resultGivenName);
			assertNotNull(resultSurname);
			assertNotNull(resultCountryCode);
			String resultStreet = smartCard.getStreet();
			String resultCity = smartCard.getCity();
			String resultPostalCode = smartCard.getPostalCode();
			LOG.debug("street: " + resultStreet);
			LOG.debug("city: " + resultCity);
			LOG.debug("postal code: " + resultPostalCode);
		} finally {
			smartCard.close();
		}
	}

	public void testAvailabilityOfBeIDConfiguration() throws Exception {
		URL url = SmartCardTest.class
				.getResource("/META-INF/safe-online-pkcs11-sc-config.properties");
		LOG.debug("URL: " + url);
		assertNotNull(url);
		assertTrue(url
				.toString()
				.matches(
						"jar:file:.*safe-online-pkcs11-sc-beid.*\\.jar!/META-INF/safe-online-pkcs11-sc-config.properties"));

		Enumeration<URL> enumerationResult = Thread.currentThread()
				.getContextClassLoader().getResources(
						"META-INF/safe-online-pkcs11-sc-config.properties");
		assertTrue(enumerationResult.hasMoreElements());
	}

	public void testIteratePKCS11Slots() throws Exception {
		// setup
		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();
		SmartCardConfig beidConfig = configFactory.getSmartCardConfigs().get(0);

		String osName = System.getProperty("os.name");
		LOG.debug("os name: " + osName);
		List<File> driverLocations = beidConfig
				.getPkcs11DriverLocations(osName);
		File existingDriverLocation = null;
		for (File driverLocation : driverLocations) {
			LOG.debug("driver location: " + driverLocation);
			if (driverLocation.exists()) {
				existingDriverLocation = driverLocation;
			}
		}
		assertNotNull(existingDriverLocation);
		LOG.debug("existing driver location: " + existingDriverLocation);
		PKCS11 pkcs11 = PKCS11.getInstance(existingDriverLocation
				.getAbsolutePath(), null, false);
		assertNotNull(pkcs11);
		try {
			CK_INFO info = pkcs11.C_GetInfo();
			String manufacturerId = new String(info.manufacturerID).trim();
			LOG.debug("manufacturer ID: " + manufacturerId);
			LOG.debug("manufacturer ID size: " + manufacturerId.length());
			long[] slotIds = pkcs11.C_GetSlotList(true);
			for (long slotId : slotIds) {
				CK_SLOT_INFO slotInfo = pkcs11.C_GetSlotInfo(slotId);
				LOG.debug("slot description: "
						+ new String(slotInfo.slotDescription));

			}
		} finally {
			pkcs11.C_Finalize(null);
		}
	}

	public void testIdentityStatement() throws Exception {
		SmartCard smartCard = SmartCardFactory.newInstance();

		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();
		smartCard.init(configFactory.getSmartCardConfigs());

		LOG.debug("Connecting to smart card...");
		smartCard.open("beid");

		String givenName = smartCard.getGivenName();
		String surname = smartCard.getSurname();
		LOG.debug("given name: " + givenName);
		LOG.debug("surname: " + surname);

		LOG.debug("Creating identity statement...");
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement("beid", smartCard);

		LOG.debug("Disconnecting from smart card...");
		smartCard.close();
		assertNotNull(identityStatement);
	}

	public void testCardRemoval() throws Exception {
		SmartCard smartCard = SmartCardFactory.newInstance();

		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();
		smartCard.init(configFactory.getSmartCardConfigs());
		smartCard.setSmartCardPinCallback(new SmartCardPinCallback() {

			public char[] getPin() {
				return SmartCardTest.getPin();
			}
		});

		LOG.debug("Connecting to smart card...");

		JOptionPane.showMessageDialog(null,
				"Please insert your BeID smart card.");

		smartCard.open("beid");

		String givenName = smartCard.getGivenName();
		String surname = smartCard.getSurname();
		LOG.debug("given name: " + givenName);
		LOG.debug("surname: " + surname);

		LOG.debug("Creating identity statement...");
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement("beid", smartCard);

		LOG.debug("Disconnecting from smart card...");
		smartCard.close();
		assertNotNull(identityStatement);

		/*
		 * Try to create an identity statement after removing/reinserting the
		 * smart card.
		 */
		JOptionPane.showMessageDialog(null,
				"Please remove and reinsert your BeID smart card.");
		smartCard.open("beid");
		PrivateKey privateKey = smartCard.getAuthenticationPrivateKey();
		LOG.debug("private key type: " + privateKey.getClass().getName());
		try {
			identityStatement = IdentityStatementFactory
					.createIdentityStatement("beid", smartCard);
		} catch (ProviderException e) {
			Throwable t = e.getCause();
			if (t instanceof PKCS11Exception) {
				smartCard.close();
				resetPKCS11Driver();
				smartCard.open("beid");
				privateKey = smartCard.getAuthenticationPrivateKey();
				identityStatement = IdentityStatementFactory
						.createIdentityStatement("beid", smartCard);
			}
		}
		smartCard.close();
	}

	public void testSmartCardConfigForWindowsXP() throws Exception {
		SmartCardConfigFactory smartCardConfigFactory = new SmartCardConfigFactoryImpl();
		List<SmartCardConfig> smartCardConfigs = smartCardConfigFactory
				.getSmartCardConfigs();
		LOG.debug("number of smart card configs: " + smartCardConfigs.size());
		assertEquals(1, smartCardConfigs.size());
		SmartCardConfig smartCardConfig = smartCardConfigs.get(0);
		LOG.debug("smart card config: " + smartCardConfig.getCardAlias());
		assertEquals("beid", smartCardConfig.getCardAlias());

		String testPlatform = "Windows XP";
		List<File> pkcs11DriverLocations = smartCardConfig
				.getPkcs11DriverLocations(testPlatform);
		LOG.debug("number of PKCS11 driver for platform " + testPlatform
				+ " = " + pkcs11DriverLocations.size());
		for (File pkcs11DriverLocation : pkcs11DriverLocations) {
			LOG.debug("PKCS#11 driver location: " + pkcs11DriverLocation
					+ " exists " + pkcs11DriverLocation.exists());
		}
	}

	public void testJniBeIdLib() throws Exception {
		Runtime runtime = Runtime.getRuntime();
		runtime.load("/usr/local/lib/libbeidlibjni.so");
		BEID_Status oStatus;
		BEID_Long CardHandle = new BEID_Long();

		oStatus = eidlib.BEID_Init(null, 0, 0, CardHandle);

		if (0 != oStatus.getGeneral()) {
			return;
		}

		BEID_Certif_Check certCheck = new BEID_Certif_Check();
		BEID_ID_Data identityData = new BEID_ID_Data();
		oStatus = eidlib.BEID_GetID(identityData, certCheck);
		LOG.debug("birth date: " + identityData.getBirthDate());
		LOG.debug("birth location: " + identityData.getBirthLocation());
		LOG.debug("sex: " + identityData.getSex());

		BEID_Address addressData = new BEID_Address();
		oStatus = eidlib.BEID_GetAddress(addressData, certCheck);
		LOG.debug("street: " + addressData.getStreet());
		LOG.debug("streetnumber: " + addressData.getStreetNumber());
		LOG.debug("municipality: " + addressData.getMunicipality());
		LOG.debug("ZIP: " + addressData.getZip());
		LOG.debug("box nr: " + addressData.getBoxNumber());

		oStatus = eidlib.BEID_FlushCache();

		oStatus = eidlib.BEID_Exit();
	}

	public void testOpenscPkcs11Driver() throws Exception {
		File tmpConfigFile = File.createTempFile("pkcs11", "conf");
		tmpConfigFile.deleteOnExit();
		PrintWriter configWriter = new PrintWriter(new FileOutputStream(
				tmpConfigFile), true);
		String name = "TestSmartCard";
		configWriter.println("name=" + name);
		configWriter.println("library=/usr/lib/opensc-pkcs11.so");
		// configWriter.println("library=/usr/local/lib/libbeidpkcs11.so");
		configWriter.println("slotListIndex=0");
		configWriter.println("showInfo=true");
		configWriter.close();
		SunPKCS11 provider = (SunPKCS11) Security.getProvider("SunPKCS11-"
				+ name);
		if (null != provider) {
			throw new RuntimeException("Smart Card provider already active");
		}
		resetPKCS11Driver();
		provider = new SunPKCS11(tmpConfigFile.getAbsolutePath());
		if (-1 == Security.addProvider(provider)) {
			throw new RuntimeException("could not add the security provider");
		}
		String providerName = provider.getName();

		CallbackHandler callbackHandler = new TestCallbackHandler();
		ProtectionParameter protectionParameter = new KeyStore.CallbackHandlerProtection(
				callbackHandler);
		KeyStore.Builder builder = KeyStore.Builder.newInstance("PKCS11",
				provider, protectionParameter);

		KeyStore keyStore = builder.getKeyStore();
		keyStore.load(null, null);

		Signature signature = Signature.getInstance("SHA1withRSA");
		PrivateKey privateKey = (PrivateKey) keyStore.getKey("Authentication",
				null);
		LOG.debug("private key: " + privateKey);
		signature.initSign(privateKey);
		String plain = "Hello World";
		byte[] plainData = plain.getBytes();
		signature.update(plainData);
		signature.sign();

		Security.removeProvider(providerName);

		JOptionPane.showMessageDialog(null,
				"Please remove and reinsert your BeID smart card.");

		// resetPKCS11Driver();

		provider = new SunPKCS11(tmpConfigFile.getAbsolutePath());
		if (-1 == Security.addProvider(provider)) {
			throw new RuntimeException("could not add the security provider");
		}

		// provider.login(null, callbackHandler);

		callbackHandler = new TestCallbackHandler();
		protectionParameter = new KeyStore.CallbackHandlerProtection(
				callbackHandler);
		builder = KeyStore.Builder.newInstance("PKCS11", provider,
				protectionParameter);

		keyStore = builder.getKeyStore();
		keyStore.load(null, null);

		signature = Signature.getInstance("SHA1withRSA");
		privateKey = (PrivateKey) keyStore.getKey("Authentication", null);
		LOG.debug("private key: " + privateKey);
		signature.initSign(privateKey);
		plain = "Hello World";
		plainData = plain.getBytes();
		signature.update(plainData);
		try {
			signature.sign();
		} catch (ProviderException e) {
			Throwable cause = e.getCause();
			if (cause instanceof PKCS11Exception) {
				Security.removeProvider(providerName);
				resetPKCS11Driver();
				provider = new SunPKCS11(tmpConfigFile.getAbsolutePath());
				if (-1 == Security.addProvider(provider)) {
					throw new RuntimeException(
							"could not add the security provider");
				}
				builder = KeyStore.Builder.newInstance("PKCS11", provider,
						protectionParameter);

				keyStore = builder.getKeyStore();
				keyStore.load(null, null);

				signature = Signature.getInstance("SHA1withRSA");
				privateKey = (PrivateKey) keyStore.getKey("Authentication",
						null);
				LOG.debug("private key: " + privateKey);
				signature.initSign(privateKey);
				plainData = plain.getBytes();
				signature.update(plainData);
				signature.sign();
			}
		}

		Security.removeProvider(providerName);
	}

	@SuppressWarnings("unchecked")
	private void resetPKCS11Driver() throws NoSuchFieldException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException, PKCS11Exception {
		Field moduleMapField = PKCS11.class.getDeclaredField("moduleMap");
		moduleMapField.setAccessible(true);
		Map<String, Object> moduleMap = (Map) moduleMapField.get(null);
		LOG.debug("moduleMap size: " + moduleMap.size());
		for (Map.Entry<String, Object> entry : moduleMap.entrySet()) {
			LOG.debug("finalizing " + entry.getKey());
			PKCS11 pkcs11 = (PKCS11) entry.getValue();
			CK_INFO info = pkcs11.C_GetInfo();
			if ("Zetes".equals(new String(info.manufacturerID).trim())) {
				Method disconnectMethod = PKCS11.class.getDeclaredMethod(
						"disconnect", new Class[] {});
				disconnectMethod.setAccessible(true);
				disconnectMethod.invoke(pkcs11, new Object[] {});
			} else {
				pkcs11.C_Finalize(null);
			}
			LOG.debug("done");
		}
		moduleMap.clear();
	}

	private static class TestCallbackHandler implements CallbackHandler {

		private static final Log LOG = LogFactory
				.getLog(TestCallbackHandler.class);

		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
			for (Callback callback : callbacks) {
				LOG.debug("callback type: " + callback.getClass().getName());
				if (callback instanceof PasswordCallback) {
					PasswordCallback passwordCallback = (PasswordCallback) callback;
					LOG.debug("password required");
					char[] pin = getPin();
					if (null == pin) {
						throw new UnsupportedCallbackException(callback,
								"User canceled PIN input.");
					}
					passwordCallback.setPassword(pin);
				}
			}
		}
	}

	private static char[] getPin() {
		JLabel promptLabel = new JLabel("Give your PIN:");

		JPasswordField passwordField = new JPasswordField(8);
		passwordField.setEchoChar('*');

		Box passwordPanel = Box.createHorizontalBox();
		passwordPanel.add(promptLabel);
		passwordPanel.add(Box.createHorizontalStrut(5));
		passwordPanel.add(passwordField);

		int result = JOptionPane.showOptionDialog(null, passwordPanel,
				"PIN Required", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (result == JOptionPane.OK_OPTION) {
			char[] pin = passwordField.getPassword();
			return pin;
		}

		return null;
	}
}
