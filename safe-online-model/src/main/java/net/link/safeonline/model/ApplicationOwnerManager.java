/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.entity.ApplicationOwnerEntity;

@Local
public interface ApplicationOwnerManager {

	ApplicationOwnerEntity getCallerApplicationOwner()
			throws ApplicationOwnerNotFoundException;
}
