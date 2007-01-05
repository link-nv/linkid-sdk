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

import javax.smartcardio.TerminalFactory;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;
import net.lin_k.safe_online.pkcs11_sc_config._1.ObjectFactory;
import net.lin_k.safe_online.pkcs11_sc_config._1.Pkcs11ScConfigType;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.impl.XmlSmartCardConfigFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.PKCS11;

public class SmartCardTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(SmartCardTest.class);

	public void testCheckPresenceOfSmartCardReader() throws Exception {
		// setup
		SmartCard smartCard = SmartCardFactory.newInstance();

		// operate & verify
		JOptionPane.showMessageDialog(null,
				"Please disconnect your smart card reader.");

		boolean disconnectResult = smartCard.isReaderPresent();
		assertFalse(disconnectResult);

		JOptionPane.showMessageDialog(null,
				"Please connect your smart card reader.");

		boolean connectResult = smartCard.isReaderPresent();
		assertTrue(connectResult);
	}

	public void testCheckSmartcardAPI() throws Exception {
		TerminalFactory terminalFactory;

		JOptionPane.showMessageDialog(null,
				"Please disconnect your smart card reader.");

		terminalFactory = TerminalFactory.getDefault();
		assertEquals(0, terminalFactory.terminals().list().size());

		JOptionPane.showMessageDialog(null,
				"Please connect your smart card reader.");

		terminalFactory = TerminalFactory.getDefault();
		assertEquals(1, terminalFactory.terminals().list().size());
	}

	public void testIsBeIDCardPresent() throws Exception {
		// setup
		SmartCard smartCard = SmartCardFactory.newInstance();
		XmlSmartCardConfigFactory factory = new XmlSmartCardConfigFactory();

		smartCard.init(factory.getSmartCardConfigs());

		JOptionPane.showMessageDialog(null, "Insert your BeID card.");

		// operate
		boolean result = smartCard.isSupportedCardPresent();
		assertTrue(result);
	}

	public void testGetCertificatesAndPrivateKeys() throws Exception {
		// setup
		SmartCard smartCard = SmartCardFactory.newInstance();
		XmlSmartCardConfigFactory configFactory = new XmlSmartCardConfigFactory();

		smartCard.init(configFactory.getSmartCardConfigs());

		// operate
		smartCard.open();

		// verify
		X509Certificate authCertResult = smartCard
				.getAuthenticationCertificate();
		X509Certificate signCertResult = smartCard.getSignatureCertificate();
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
	}

	public void testAvailabilityOfBeIDConfiguration() throws Exception {
		URL url = SmartCardTest.class
				.getResource("/META-INF/safe-online-pkcs11-sc-config.xml");
		LOG.debug("URL: " + url);
		assertNotNull(url);
		assertTrue(url
				.toString()
				.matches(
						"jar:file:.*safe-online-pkcs11-sc-beid.*\\.jar!/META-INF/safe-online-pkcs11-sc-config.xml"));

		Enumeration<URL> enumerationResult = Thread.currentThread()
				.getContextClassLoader().getResources(
						"META-INF/safe-online-pkcs11-sc-config.xml");
		assertTrue(enumerationResult.hasMoreElements());
	}

	public void testSchemaValidBeIDConfiguration() throws Exception {
		URL url = SmartCardTest.class
				.getResource("/META-INF/safe-online-pkcs11-sc-config.xml");
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<Pkcs11ScConfigType> configElement = (JAXBElement<Pkcs11ScConfigType>) unmarshaller
				.unmarshal(url);
		assertNotNull(configElement);
		Pkcs11ScConfigType config = configElement.getValue();
		assertEquals("beid", config.getAlias());
	}

	public void testIteratePKCS11Slots() throws Exception {
		// setup
		XmlSmartCardConfigFactory configFactory = new XmlSmartCardConfigFactory();
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
		CK_C_INITIALIZE_ARGS ck_c_initialize_args = new CK_C_INITIALIZE_ARGS();
		PKCS11 pkcs11 = PKCS11.getInstance(existingDriverLocation
				.getAbsolutePath(), "C_GetFunctionList", ck_c_initialize_args,
				false);
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
}
