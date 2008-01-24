/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service.bean;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.service.PublicApplicationService;

/**
 * <h2>{@link PublicApplicationServiceBean} - Service for
 * {@link PublicApplication}.</h2>
 *
 * <p>
 * Provides access to attributes of the given application that are publicly
 * available.
 * </p>
 * <p>
 * <i>Dec 18, 2007</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
public class PublicApplicationServiceBean implements PublicApplicationService {

	@EJB
	private ApplicationDAO applicationDAO;

	@PermitAll
	public PublicApplication getPublicApplication(String applicationName)
			throws ApplicationNotFoundException {

		return new PublicApplication(this.applicationDAO
				.getApplication(applicationName));
	}
}
