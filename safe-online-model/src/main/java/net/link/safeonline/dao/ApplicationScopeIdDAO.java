/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationScopeIdEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Application Scope Id entity data access object interface definition.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface ApplicationScopeIdDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ApplicationScopeIdDAOBean/local";


    ApplicationScopeIdEntity findApplicationScopeId(SubjectEntity subject, ApplicationEntity application);

    ApplicationScopeIdEntity addApplicationScopeId(SubjectEntity subject, ApplicationEntity application);

    ApplicationScopeIdEntity findApplicationScopeId(String applicationUserId);

    void removeApplicationScopeIds(SubjectEntity subject);

    void removeApplicationScopeIds(ApplicationEntity application);

}
