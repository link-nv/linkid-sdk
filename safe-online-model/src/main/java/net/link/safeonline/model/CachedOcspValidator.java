/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.entity.pkix.TrustDomainEntity;

@Local
public interface CachedOcspValidator {

	/**
	 * Given an X509 certificate and its issuerCertificate, validates the
	 * certificate using OCSP
	 * 
	 * @param certificate
	 * @param issuerCertificate
	 * @return
	 */
	boolean performCachedOcspCheck(TrustDomainEntity trustDomain,
			X509Certificate certificate, X509Certificate issuerCertificate);

}
