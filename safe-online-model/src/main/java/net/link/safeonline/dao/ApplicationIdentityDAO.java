/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeTypeEntity;

@Local
public interface ApplicationIdentityDAO {

	void addApplicationIdentity(ApplicationEntity application,
			long identityVersion, List<AttributeTypeEntity> attributeTypes);

	ApplicationIdentityEntity getApplicationIdentity(
			ApplicationEntity application, long identityVersion)
			throws ApplicationIdentityNotFoundException;

	List<ApplicationIdentityEntity> getApplicationIdentities(
			ApplicationEntity application);

	void removeApplicationIdentity(ApplicationIdentityEntity applicationIdentity);
}
