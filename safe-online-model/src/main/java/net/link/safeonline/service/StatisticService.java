/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;

@Local
public interface StatisticService {

	public StatisticEntity getStatistic(String statisticName,
			String applicationName) throws StatisticNotFoundException,
			PermissionDeniedException;

	public List<StatisticEntity> getStatistics(ApplicationEntity application)
			throws PermissionDeniedException;

}
