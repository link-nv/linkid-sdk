/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.entity.helpdesk.LogLevelType;

@Local
public interface HelpdeskEventDAO {

	void persist(List<HelpdeskEventEntity> helpdeskEvents);

	List<HelpdeskEventEntity> listLogs(Long logId);

	void clearEvents(long ageInMinutes, LogLevelType logLevel);

}
