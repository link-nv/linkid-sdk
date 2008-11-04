/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;


@Local
public interface HelpdeskManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/HelpdeskManagerBean/local";


    public Long persist(String location, List<HelpdeskEventEntity> helpdeskEventList);

    public int getHelpdeskContextLimit();

}
