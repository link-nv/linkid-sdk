/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;

/**
 * Authentication service interface.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface AuthenticationService {
	/**
	 * Authenticates a user for a certain application. This method is used by
	 * the authentication web service.
	 * 
	 * @param applicationName
	 * @param login
	 * @param password
	 * @return <code>true</code> if the user was authenticated correctly,
	 *         <code>false</code> otherwise.
	 */
	boolean authenticate(String applicationName, String login, String password);

	/**
	 * Authenticates a user without any application subscription check. This
	 * method is used by the SafeOnline core JAAS login module.
	 * 
	 * @param login
	 * @param password
	 * @return <code>true</code> if the user was authenticated correctly,
	 *         <code>false</code> otherwise.
	 */
	boolean authenticate(String login, String password);

	/**
	 * Authenticates a user via an authentication statement. The given session
	 * Id must match the one given in the authentication statement. The session
	 * Id is managed by the servlet front-end container.
	 * 
	 * @param sessionId
	 * @param authenticationStatementData
	 * @return the user Id
	 * @throws ArgumentIntegrityException
	 * @throws TrustDomainNotFoundException
	 * @throws SubjectNotFoundException
	 *             in case the certificate was not linked to any subject.
	 * @throws SubscriptionNotFoundException
	 *             in case the subject is not subscribed to the application.
	 * @throws ApplicationNotFoundException
	 *             in case the application does not exist.
	 */
	String authenticate(String sessionId, byte[] authenticationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException, SubscriptionNotFoundException,
			ApplicationNotFoundException;
}
