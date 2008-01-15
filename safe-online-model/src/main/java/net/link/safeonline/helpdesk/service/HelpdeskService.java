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
	 */
	List<HelpdeskContextEntity> listContexts();

	/**
	 * Gives all helpdesk events logs for the specified log id.
	 * 
	 * @param logId
	 */
	List<HelpdeskEventEntity> listEvents(Long logId);

	/**
	 * Gives all users with helpdesk event logs
	 * 
	 */
	List<String> listUsers();

	/**
	 * Gives all helpdesk logs for a specific user
	 * 
	 * @param user
	 */
	List<HelpdeskContextEntity> listUserContexts(String user);

	/**
	 * Removes the helpdesk context log and its associated events for the
	 * specified log id.
	 * 
	 * @param logId
	 */
	boolean removeLog(Long logId);

}
