/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import javax.ejb.Local;

@Local
public interface Timeout {

	public static final String APPLICATION_COOKIE = "ApplicationCookie";

	public static final String TIMEOUT_COOKIE = "TimeoutCookie";

	public static final String ENTRY_COOKIE = "EntryCookie";

	/*
	 * Accessors.
	 */
	String getApplicationUrl();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
