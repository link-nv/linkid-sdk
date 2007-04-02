/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner;

import javax.ejb.Local;

@Local
public interface Charts {

	/*
	 * Factories
	 */
	void statListFactory();

	/*
	 * Actions.
	 */
	String viewStat();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
