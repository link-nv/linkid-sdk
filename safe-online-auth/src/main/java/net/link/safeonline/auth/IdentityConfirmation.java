/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.data.AttributeDO;

@Local
public interface IdentityConfirmation {

	/*
	 * Actions.
	 */
	String agree();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factory.
	 */
	List<AttributeDO> identityConfirmationListFactory();
}
