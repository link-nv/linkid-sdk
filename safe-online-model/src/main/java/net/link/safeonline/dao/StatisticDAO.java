/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;

@Local
public interface StatisticDAO {

	StatisticEntity addStatistic(String name, ApplicationEntity application);

	StatisticEntity findStatisticById(long statisticId);

	StatisticEntity findStatisticByNameAndApplication(String name,
			ApplicationEntity application);

	List<StatisticEntity> listStatistics(ApplicationEntity application);

}
