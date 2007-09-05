/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import javax.ejb.Local;

@Local
public interface HelpdeskLog {
	/*
	 * Factories.
	 */
	void helpdeskContextListFactory();

	void helpdeskLogListFactory();

	/*
	 * Accessors.
	 */

	/*
	 * Actions.
	 */
	String view();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
