/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner;

import java.io.IOException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.PermissionDeniedException;

@Local
public interface Charts {

	/*
	 * Factories
	 */
	void statListFactory() throws PermissionDeniedException;

	/*
	 * Actions.
	 */
	String viewStat();

	String export() throws IOException;

	String exportStat() throws IOException;

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
