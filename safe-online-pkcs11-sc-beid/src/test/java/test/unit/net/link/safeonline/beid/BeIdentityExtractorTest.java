/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.beid;

import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.beid.BeIdentityExtractor;
import net.link.safeonline.p11sc.spi.IdentityDataCollector;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BeIdentityExtractorTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(BeIdentityExtractorTest.class);

	private BeIdentityExtractor testedInstance;

	private TestIdentityDataCollector testIdentityDataCollector;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new BeIdentityExtractor();

		this.testIdentityDataCollector = new TestIdentityDataCollector();
		this.testedInstance.init(this.testIdentityDataCollector);
	}

	private static class TestIdentityDataCollector implements
			IdentityDataCollector {

		private String countryCode;

		private String givenName;

		private String surname;

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
			throw new NotImplementedException();
		}

		public void setPostalCode(String postalCode) {
			throw new NotImplementedException();
		}

		public void setStreet(String street) {
			throw new NotImplementedException();
		}
	}

	public void testPostPkcs11() throws Exception {
		// setup
		X509Certificate authCert2006 = PkiTestUtils
				.loadCertificateFromResource("/fcorneli-auth.crt");
		LOG.debug("test authentication certificate 2006: " + authCert2006);
		X509Certificate authCert2004 = PkiTestUtils
				.loadCertificateFromResource("/gdesmedt-auth.crt");
		LOG.debug("test authentication certificate 2004: " + authCert2004);

		// operate
		this.testedInstance.postPkcs11(authCert2006);

		// verify
		LOG.debug("given name: " + this.testIdentityDataCollector.givenName);
		assertEquals("Frank Henri", this.testIdentityDataCollector.givenName);
		assertEquals("Cornelis", this.testIdentityDataCollector.surname);
		assertEquals("BE", this.testIdentityDataCollector.countryCode);

		// operate
		this.testedInstance.postPkcs11(authCert2004);

		// verify
		LOG.debug("given name: " + this.testIdentityDataCollector.givenName);
		assertEquals("Griet", this.testIdentityDataCollector.givenName);
		assertEquals("De Smedt", this.testIdentityDataCollector.surname);
	}
}
