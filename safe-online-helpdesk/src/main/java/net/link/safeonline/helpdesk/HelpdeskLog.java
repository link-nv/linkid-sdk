/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import java.util.List;

import javax.ejb.Local;

@Local
public interface HelpdeskLog {
	/*
	 * Factories.
	 */
	void helpdeskContextListFactory();

	void helpdeskLogListFactory();

	void helpdeskUserListFactory();

	void helpdeskUserContextListFactory();

	/*
	 * Richfaces
	 */
	List autocomplete(Object event);

	/*
	 * Accessors.
	 */
	Long getSearchId();

	void setSearchId(Long searchId);

	/*
	 * Actions.
	 */
	String search();

	String view();

	String removeLog();

	String viewUser();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
