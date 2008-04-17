/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.statement;

/**
 * Interface for identity provider component.
 * 
 * @author fcorneli
 * 
 */
public interface IdentityProvider {

	/**
	 * Gives back the given name.
	 * 
	 * @return
	 */
	String getGivenName();

	/**
	 * Gives back the surname.
	 * 
	 * @return
	 */
	String getSurname();
}
