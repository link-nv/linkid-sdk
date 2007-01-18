/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;

/**
 * Validator for PKI.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface PkiValidator {

	/**
	 * Validates the given X509 certificate using the trust points (and policy)
	 * defined within a certain trust domain.
	 * 
	 * @param trustDomainName
	 * @param certificate
	 * @return
	 */
	boolean validateCertificate(String trustDomainName,
			X509Certificate certificate) throws TrustDomainNotFoundException;
}
