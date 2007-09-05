/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import java.util.List;

import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.entity.helpdesk.LogLevelType;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;

public interface HelpdeskManager {

	/*
	 * Create a new Helpdesk Context on the HttpSession
	 */
	public void clear();

	/*
	 * Get current Helpdesk Context from the HttpSession
	 */
	public List<HelpdeskEventEntity> getCurrent()
			throws HelpdeskContextNotFoundException;

	/*
	 * Add new helpdesk event to the helpdesk context
	 */
	public void add(String msg, LogLevelType logLevel)
			throws HelpdeskContextNotFoundException;

	/*
	 * Persist the volatile helpdesk context, return the id
	 */
	public Long persistContext() throws HelpdeskContextNotFoundException;

}
