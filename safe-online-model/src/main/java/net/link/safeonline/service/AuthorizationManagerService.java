/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.RoleNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;

@Local
public interface AuthorizationManagerService {

	/**
	 * Gives back a set of roles for a given login. The assignment of these
	 * roles to a certain principal depends on the security measures against
	 * attacks of the SafeOnline core.
	 * 
	 * @param login
	 * @return
	 * @throws SubjectNotFoundException
	 */
	Set<String> getRoles(String login) throws SubjectNotFoundException;

	/**
	 * Gives back all available roles supported by the system.
	 * 
	 * @return
	 */
	Set<String> getAvailableRoles();

	/**
	 * Update the roles for the given subject.
	 * 
	 * @param login
	 * @param roles
	 * @throws RoleNotFoundException
	 */
	void setRoles(String login, Set<String> roles)
			throws SubjectNotFoundException, RoleNotFoundException;
}
