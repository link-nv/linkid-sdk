/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Spike to test out the usability of the Java CertPath API when using
 * non-global-CA certificates.
 * 
 * @author fcorneli
 * 
 */
public class CertPathApiTest {

	private static final Log LOG = LogFactory.getLog(CertPathApiTest.class);

	@Test
	public void validating() throws Exception {

		// Security.addProvider(new BouncyCastleProvider());

		/*
		 * Root CA
		 */
		KeyPair rootCaKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate rootCaCert = PkiTestUtils
				.generateSelfSignedCertificate(rootCaKeyPair, "CN=RootCA");
		CertificateFactory certificateFactory = CertificateFactory
				.getInstance("X.509");
		LOG.debug("certificate factory provider: "
				+ certificateFactory.getProvider().getName());
		LOG.debug("root CA cert type: " + rootCaCert.getClass().getName());
		/*
		 * rootCaCert = (X509Certificate) certificateFactory
		 * .generateCertificate(new ByteArrayInputStream(rootCaCert
		 * .getEncoded()));
		 */
		LOG.debug("root CA cert type: " + rootCaCert.getClass().getName());

		/*
		 * Intermediate CA
		 */
		KeyPair interCaKeyPair = PkiTestUtils.generateKeyPair();
		DateTime notBefore = new DateTime();
		DateTime notAfter = notBefore.plusYears(1);
		X509Certificate interCaCert = PkiTestUtils.generateCertificate(
				interCaKeyPair.getPublic(), "CN=InterCA", rootCaKeyPair
						.getPrivate(), rootCaCert, notBefore, notAfter, null,
				true, false, null);
		/*
		 * interCaCert = (X509Certificate) certificateFactory
		 * .generateCertificate(new ByteArrayInputStream(interCaCert
		 * .getEncoded()));
		 */
		LOG.debug("inter CA cert type: " + interCaCert.getClass().getName());

		/*
		 * Entity
		 */
		KeyPair entityKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate entityCert = PkiTestUtils.generateCertificate(
				entityKeyPair.getPublic(), "CN=Entity", interCaKeyPair
						.getPrivate(), interCaCert, notBefore, notAfter, null,
				false, false, null);
		entityCert = (X509Certificate) certificateFactory
				.generateCertificate(new ByteArrayInputStream(entityCert
						.getEncoded()));
		LOG.debug("entity cert type: " + entityCert.getClass().getName());

		CertPathValidator certPathValidator = CertPathValidator
				.getInstance("PKIX");
		LOG.debug("cert path validator provider: "
				+ certPathValidator.getProvider().getName());

		TrustAnchor rootAnchor = new TrustAnchor(rootCaCert, null);
		TrustAnchor interAnchor = new TrustAnchor(interCaCert, null);
		Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
		trustAnchors.add(rootAnchor);
		trustAnchors.add(interAnchor);

		X509CertSelector certSelector = new X509CertSelector();
		certSelector.setCertificate(entityCert);
		PKIXBuilderParameters certPathParams = new PKIXBuilderParameters(
				trustAnchors, certSelector);
		certPathParams.setRevocationEnabled(false);
		CertPathBuilder certPathBuilder = CertPathBuilder.getInstance("PKIX");
		LOG.debug("cert path builder provider: "
				+ certPathBuilder.getProvider().getName());
		CertPathBuilderResult certPathBuilderResult = certPathBuilder
				.build(certPathParams);
		CertPath certPath = certPathBuilderResult.getCertPath();
		for (Certificate cert : certPath.getCertificates()) {
			X509Certificate x509Cert = (X509Certificate) cert;
			LOG.debug("x509 subject: " + x509Cert.getSubjectDN());
		}

		PKIXParameters params = new PKIXParameters(trustAnchors);
		params.setRevocationEnabled(false);
		PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) certPathValidator
				.validate(certPath, params);
		LOG.debug("result trust anchor: "
				+ result.getTrustAnchor().getTrustedCert());
	}
}
