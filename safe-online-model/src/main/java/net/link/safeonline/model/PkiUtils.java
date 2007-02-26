/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.ejb.EJBException;

import net.link.safeonline.authentication.exception.CertificateEncodingException;

/**
 * PKIX utility methods.
 * 
 * @author fcorneli
 * 
 */
public class PkiUtils {

	private PkiUtils() {
		// empty
	}

	public static X509Certificate decodeCertificate(byte[] encodedCertificate)
			throws CertificateEncodingException {
		if (null == encodedCertificate) {
			return null;
		}
		CertificateFactory certificateFactory;
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			throw new EJBException("certificate factory error: "
					+ e.getMessage());
		}
		InputStream certInputStream = new ByteArrayInputStream(
				encodedCertificate);
		try {
			X509Certificate certificate = (X509Certificate) certificateFactory
					.generateCertificate(certInputStream);
			return certificate;
		} catch (CertificateException e) {
			throw new CertificateEncodingException();
		}
	}
}
