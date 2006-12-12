/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.entity.HistoryEntity;

/**
 * Interface of service component to access the identity data of a caller
 * subject.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface IdentityService {

	/**
	 * Gives back the full name of the user linked to the caller principal.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Saves the full name of the user linked to the caller principal.
	 * 
	 * @param name
	 */
	void saveName(String name);

	/**
	 * Gives back the authentication history of the user linked to the caller
	 * principal.
	 * 
	 * @return a list of history entries.
	 */
	List<HistoryEntity> getHistory();
}
