/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.service.AttributeDO;

@Local
public interface IdentityConfirmation {

	String agree();

	void destroyCallback();

	List<AttributeDO> identityConfirmationListFactory();
}
