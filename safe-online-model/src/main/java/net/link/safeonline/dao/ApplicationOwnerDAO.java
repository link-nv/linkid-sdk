/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface ApplicationOwnerDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ApplicationOwnerDAOBean/local";

    ApplicationOwnerEntity findApplicationOwner(String name);

    ApplicationOwnerEntity getApplicationOwner(String name) throws ApplicationOwnerNotFoundException;

    ApplicationOwnerEntity addApplicationOwner(String name, SubjectEntity admin);

    void removeApplicationOwner(String name);

    List<ApplicationOwnerEntity> listApplicationOwners();

    ApplicationOwnerEntity getApplicationOwner(SubjectEntity adminSubject) throws ApplicationOwnerNotFoundException;

    ApplicationOwnerEntity findApplicationOwner(SubjectEntity adminSubject);

    void removeApplication(ApplicationEntity application);
}
