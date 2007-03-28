/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;

/**
 * Interface of service that manages the credentials of the caller subject.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface CredentialService {

	/**
	 * Changes the password of the current user. Of course for that to happen
	 * the oldPassword must match.
	 * 
	 * @param oldPassword
	 * @param newPassword
	 * @throws PermissionDeniedException
	 */
	void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException;

	/**
	 * Merges the identity statement with the current user subject. The identity
	 * statement has been generated by the client-side web application component
	 * and is cryptographically signed by the user's authentication certificate.
	 * 
	 * @param identityStatementData
	 *            the identity statement.
	 * @throws TrustDomainNotFoundException
	 * @throws PermissionDeniedException
	 *             in case the identity statement user does not correspond with
	 *             the active principal.
	 * @throws ArgumentIntegrityException
	 *             in case the identity statement didn't pass the integrity
	 *             checks.
	 * @throws AttributeTypeNotFoundException
	 */
	void mergeIdentityStatement(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException;
}
