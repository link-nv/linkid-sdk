/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

/**
 * Interface for PKI providers. An example of a PKI provider could be the BeID
 * PKIX provider.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface PkiProvider {

	/**
	 * Determines whether this PKI provider could process the given certificate.
	 * 
	 * @param certificate
	 * @return
	 */
	boolean accept(X509Certificate certificate);

	/**
	 * Gives back the name of the trust domain that this PKI provider requires
	 * for certificate validation.
	 * 
	 * @return
	 */
	String getTrustDomainName();
}
