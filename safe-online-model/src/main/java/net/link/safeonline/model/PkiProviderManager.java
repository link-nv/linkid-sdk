/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.entity.TrustDomainEntity;

/**
 * Interface for component that manages the different PKI providers.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface PkiProviderManager {

	public static String PKI_PROVIDER_JNDI = "SafeOnline/pkix";

	/**
	 * Finds the trust domain that could possibly validate the given
	 * certificate. If there exists to trust domain within the system that could
	 * handle the certificate's validation process, this method returns
	 * <code>null</code>.
	 * 
	 * @param certificate
	 * @return the trust domain, or <code>null</code> if no existing trust
	 *         domain could handle the certificate's validation.
	 */
	TrustDomainEntity findTrustDomain(X509Certificate certificate);
}
