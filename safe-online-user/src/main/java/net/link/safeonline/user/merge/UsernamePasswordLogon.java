/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.merge;

import javax.ejb.Local;

@Local
public interface UsernamePasswordLogon {

	/*
	 * Accessors.
	 */
	String getSource();

	String getPassword();

	void setPassword(String password);

	/*
	 * Actions.
	 */
	String login();

	/*
	 * Lifecycle.
	 */
	void init();

	void destroyCallback();
}
