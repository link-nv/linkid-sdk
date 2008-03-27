/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid;

import javax.ejb.Local;

@Local
public interface Main {
	/*
	 * Accessor
	 */
	String getRedirectUrl();

	/*
	 * Lifecycle callbacks
	 */
	void initCallback();

	void destroyCallback();
}
