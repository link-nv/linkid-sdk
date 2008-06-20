/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;

@Local
public interface Identity {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factories.
	 */
	void attributeListFactory() throws AttributeTypeNotFoundException,
			PermissionDeniedException;

	/*
	 * Accessors
	 */
	String getUsername();

	/*
	 * Actions.
	 */
	String edit();

	String removeAttribute() throws AttributeTypeNotFoundException,
			PermissionDeniedException, AttributeNotFoundException;

	String add();
}
