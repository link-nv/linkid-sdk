/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface AttributeAdd {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Actions.
	 */
	String commit();

	/*
	 * Factories.
	 */
	void attributeEditContextFactory();
}
