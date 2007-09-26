/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.application;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.ApplicationEntity;

/**
 * Domain Model class for Application.
 * 
 * @author fcorneli
 * 
 */
public class Application {

	private final ApplicationEntity entity;

	/**
	 * Main constructor.
	 * 
	 * @param entity
	 */
	public Application(ApplicationEntity entity) {
		this.entity = entity;
	}

	/**
	 * Check whether a user is allowed to subscribe onto this application.
	 * 
	 * @throws PermissionDeniedException
	 */
	public void checkUserSubscriptionPermission()
			throws PermissionDeniedException {
		if (false == this.entity.isAllowUserSubscription()) {
			throw new PermissionDeniedException("user not allowed to subscribe");
		}
	}

	/**
	 * Gives back the underlying persistent application entity.
	 * 
	 * @return
	 */
	public ApplicationEntity getEntity() {
		return this.entity;
	}
}
