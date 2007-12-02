/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;

@Local
public interface StatisticDataPointDAO {

	StatisticDataPointEntity addStatisticDataPoint(String name,
			StatisticEntity statistic, long x, long y, long z);

	List<StatisticDataPointEntity> listStatisticDataPoints(String name,
			StatisticEntity statistic);

	StatisticDataPointEntity findOrAddStatisticDataPoint(String name,
			StatisticEntity statistic);

	void cleanStatisticDataPoints(StatisticEntity statistic);

	void cleanStatisticDataPoints(StatisticEntity statistic, Date ageLimit);

}
