/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Register {
	// actions
	String register();

	// fields
	String getLogin();

	void setLogin(String login);

	String getPassword();

	void setPassword(String password);

	String getName();

	void setName(String name);

	void destroyCallback();
}
