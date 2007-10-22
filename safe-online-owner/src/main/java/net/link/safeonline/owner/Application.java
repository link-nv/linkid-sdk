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

	/*
	 * Accessors
	 */

	/*
	 * Factories
	 */
	void applicationListFactory();

	void usageAgreementListFactory();

	/*
	 * Actions
	 */
	String view();

	String edit();

	String save();

	String viewStats();

	void allowedDevices();

	String viewUsageAgreement();

	String editUsageAgreement();

	/*
	 * Lifecycle
	 */
	void destroyCallback();

}
