/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.beid;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.beid.BeIdentityExtractor;
import net.link.safeonline.p11sc.spi.IdentityDataCollector;

import org.apache.commons.io.IOUtils;
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
		CertificateFactory certificateFactory = CertificateFactory
				.getInstance("x.509");
		InputStream certInputStream = BeIdentityExtractorTest.class
				.getResourceAsStream("/fcorneli-auth.crt");
		X509Certificate authCert;
		try {
			authCert = (X509Certificate) certificateFactory
					.generateCertificate(certInputStream);
		} finally {
			IOUtils.closeQuietly(certInputStream);
		}
		LOG.debug("test authentication certificate: " + authCert);

		// operate
		this.testedInstance.postPkcs11(authCert);

		// verify
		LOG.debug("given name: " + this.testIdentityDataCollector.givenName);
		assertEquals("Frank Henri", this.testIdentityDataCollector.givenName);
		assertEquals("Cornelis", this.testIdentityDataCollector.surname);
		assertEquals("BE", this.testIdentityDataCollector.countryCode);
	}
}
