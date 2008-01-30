/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.pkix;

import static org.junit.Assert.assertNotNull;

import java.security.Security;
import java.security.cert.X509Certificate;

import net.link.safeonline.sdk.trust.PkiClient;
import net.link.safeonline.sdk.trust.PkiClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class PkiClientTest {

	private static final String OLAS_LOCATION = "localhost:8080";

	private static final Log LOG = LogFactory.getLog(PkiClientTest.class);

	@BeforeClass
	public static void setUp() {
		if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	@Test
	public void testGetCertificate() throws Exception {
		// operate
		PkiClient pkiClient = new PkiClientImpl(OLAS_LOCATION);

		X509Certificate certificate = pkiClient.getSigningCertificate();

		// verify
		assertNotNull(certificate);
		LOG.debug("OLAS certificate: " + certificate);
	}
}
