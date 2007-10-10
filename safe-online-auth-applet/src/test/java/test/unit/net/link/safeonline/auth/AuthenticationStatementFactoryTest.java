/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.auth.AuthenticationStatementFactory;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardPinCallback;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.util.ASN1Dump;

public class AuthenticationStatementFactoryTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationStatementFactoryTest.class);

	public void testCreateAuthenticationStatement() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate cert = PkiTestUtils.generateSelfSignedCertificate(
				keyPair, "CN=Test");
		String sessionId = UUID.randomUUID().toString();
		String applicationId = "test-application-id";
		SmartCard smartCard = new TestSmartCard(cert, keyPair);

		// operate
		byte[] result = AuthenticationStatementFactory
				.createAuthenticationStatement(sessionId, applicationId,
						smartCard);

		// verify
		assertNotNull(result);
		LOG.debug("authentication statement: "
				+ ASN1Dump.dumpAsString(ASN1Object.fromByteArray(result)));
	}

	// TODO: create a SmartCardAdapter?
	private static class TestSmartCard implements SmartCard {

		private final X509Certificate authCert;

		private final PrivateKey authPrivateKey;

		public TestSmartCard(X509Certificate authCert, KeyPair authKeyPair) {
			this.authCert = authCert;
			this.authPrivateKey = authKeyPair.getPrivate();
		}

		public void close() {
		}

		public X509Certificate getAuthenticationCertificate() {
			return this.authCert;
		}

		public PrivateKey getAuthenticationPrivateKey() {
			return this.authPrivateKey;
		}

		public String getCity() {
			return null;
		}

		public String getCountryCode() {
			return null;
		}

		public String getGivenName() {
			return null;
		}

		public String getPostalCode() {
			return null;
		}

		public X509Certificate getSignatureCertificate() {
			return null;
		}

		public PrivateKey getSignaturePrivateKey() {
			return null;
		}

		public String getStreet() {
			return null;
		}

		public String getSurname() {
			return null;
		}

		public void init(@SuppressWarnings("unused")
		List<SmartCardConfig> smartCardConfigs) {
			// empty
		}

		public boolean isOpen() {
			return false;
		}

		public void open(@SuppressWarnings("unused")
		String smartCardAlias) {
			// empty
		}

		public void setSmartCardPinCallback(@SuppressWarnings("unused")
		SmartCardPinCallback smartCardPinCallback) {
			// empty
		}

		public List<X509Certificate> getAuthenticationCertificatePath() {
			return null;
		}

		public void resetPKCS11Driver() {
		}
	}
}
