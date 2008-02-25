/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.backend;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

@Local
public interface CredentialManager {

	String authenticate(String sessionId,
			AuthenticationStatement authenticationStatement)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException;

	void mergeIdentityStatement(String deviceUserId,
			byte[] identityStatementData) throws TrustDomainNotFoundException,
			PermissionDeniedException, ArgumentIntegrityException,
			AttributeTypeNotFoundException;

	void removeIdentity(String deviceUserId, byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException,
			SubjectNotFoundException;
}
