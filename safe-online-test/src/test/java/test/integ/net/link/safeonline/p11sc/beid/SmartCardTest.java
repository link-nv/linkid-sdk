/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.p11sc.beid;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardConfigFactory;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.impl.SmartCardConfigFactoryImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
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
}
