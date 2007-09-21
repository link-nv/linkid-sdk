/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

@Local
public interface Authorization {

	/*
	 * Accessors.
	 */
	String getUser();

	void setUser(String user);

	List<String> getRoles();

	void setRoles(List<String> roles);

	/*
	 * Actions.
	 */
	String search();

	String cancel();

	String save();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	void postConstructCallback();

	/*
	 * Factories.
	 */
	List<SelectItem> availableRolesFactory();
}
