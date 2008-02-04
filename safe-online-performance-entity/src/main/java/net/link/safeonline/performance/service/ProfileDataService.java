/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service;

import javax.ejb.Local;

import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.service.bean.ProfileDataServiceBean;
import net.link.safeonline.util.performance.ProfileData;

/**
 * <h2>{@link ProfileDataServiceBean} - Service bean for
 * {@link ProfileDataEntity}.</h2>
 *
 * <p>
 * Create {@link ProfileDataEntity}s from {@link ProfileData}.
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface ProfileDataService {

	public static final String BINDING = "SafeOnline/ProfileDataService";

	/**
	 * Add the given data to the database.
	 */
	public ProfileDataEntity addData(ProfileData data, AgentTimeEntity agentTime);

}
