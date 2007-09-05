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

@Local
public interface HelpdeskEventDAO {

	void addHelpdeskEvent(HelpdeskEventEntity helpdeskEvent);

	List<HelpdeskEventEntity> listLogs(Long logId);

}
