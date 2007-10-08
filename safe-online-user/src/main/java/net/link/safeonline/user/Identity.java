/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Identity {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factories.
	 */
	void attributeListFactory();

	/*
	 * Accessors
	 */
	String getUsername();

	/*
	 * Actions.
	 */
	String edit();

	String removeAttribute();

	String add();
}
