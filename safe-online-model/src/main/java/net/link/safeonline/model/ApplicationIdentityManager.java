/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;

/**
 * Interface for the application identity manager component. This component
 * manages the lifecycle of an application identity.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationIdentityManager {

	/**
	 * Updates the identity of an application. This COULD cause the application
	 * to receive a new identity version. In case of a new identity version each
	 * application user will have to reconfirm the usafe of application identity
	 * attributes.
	 * 
	 * @param applicationId
	 * @param applicationIdentityAttributeTypes
	 * @throws ApplicationNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 * @throws AttributeTypeNotFoundException
	 */
	void updateApplicationIdentity(String applicationId,
			String[] applicationIdentityAttributeTypes)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException,
			AttributeTypeNotFoundException;

}
