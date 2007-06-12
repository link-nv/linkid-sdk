/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.authentication.service.bean.IdentityStatementAttributes;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

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
	 * Gives back the trust domain that this PKI provider requires for
	 * certificate validation.
	 * 
	 * @return
	 */
	TrustDomainEntity getTrustDomain() throws TrustDomainNotFoundException;

	/**
	 * Gives back a reference to this EJB session object.
	 * 
	 * @return
	 */
	PkiProvider getReference();

	/**
	 * Maps from an identity statement attribute to a core attribute type.
	 * 
	 * @param identityStatementAttributes
	 * @return
	 */
	String mapAttribute(IdentityStatementAttributes identityStatementAttributes);

	/**
	 * Stores additional attributes. This callback method allows for PKI
	 * providers to store additional attributes related to their specific
	 * device.
	 * 
	 * @param certificate
	 */
	void storeAdditionalAttributes(X509Certificate certificate);

	/**
	 * Gives back the identifier domain name.
	 * 
	 * @return
	 */
	String getIdentifierDomainName();

	/**
	 * Gives back the subject identifier. This identifier should be unique
	 * within the identifier domain.
	 * 
	 * @param certificate
	 * @return
	 */
	String getSubjectIdentifier(X509Certificate certificate);
}
