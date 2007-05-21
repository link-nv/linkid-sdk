/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner;

import javax.ejb.Local;

@Local
public interface Application {

	void applicationListFactory();

	String view();

	void destroyCallback();

	String edit();

	String save();

	String viewStats();

	void allowedDevices();
}
