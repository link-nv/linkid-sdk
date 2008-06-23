/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;

@Local
public interface Application {

	/*
	 * Accessors
	 */

	/*
	 * Factories
	 */
	void applicationListFactory();

	void usageAgreementListFactory() throws ApplicationNotFoundException,
			PermissionDeniedException;

	/*
	 * Actions
	 */
	String view() throws ApplicationNotFoundException,
			PermissionDeniedException, ApplicationIdentityNotFoundException;

	String edit();

	String save() throws ApplicationNotFoundException,
			PermissionDeniedException;

	String viewStats();

	void allowedDevices();

	String viewUsageAgreement();

	String editUsageAgreement() throws ApplicationNotFoundException,
			PermissionDeniedException;

	/*
	 * Lifecycle
	 */
	void destroyCallback();

}
