/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;

/**
 * Interface to service for retrieving information about helpdesk logs.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface HelpdeskService {

	/**
	 * Gives back all available helpdesk log contexts.
	 * 
	 * @return
	 */
	List<HelpdeskContextEntity> listContexts();

	/**
	 * Gives back all available helpdesk events logs for a specified log id.
	 * 
	 * @return
	 */
	List<HelpdeskEventEntity> listLogs(Long logId);

}
