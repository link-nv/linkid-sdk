/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface ApplicationOwnerDAO {

	ApplicationOwnerEntity findApplicationOwner(String name);

	ApplicationOwnerEntity getApplicationOwner(String name)
			throws ApplicationOwnerNotFoundException;

	ApplicationOwnerEntity addApplicationOwner(String name, SubjectEntity admin);

	List<ApplicationOwnerEntity> listApplicationOwners();

	ApplicationOwnerEntity getApplicationOwner(SubjectEntity adminSubject);
}
