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

	public static String PKI_PROVIDER_JNDI = "SafeOnline/pkix";

	/**
	 * Determines whether this PKI provider COULD process the given certificate.
	 * This method should not perform a complete certificate validation, that's
	 * up to the PKI validator component.
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
