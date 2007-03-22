package net.link.safeonline.service;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.StatisticEntity;

@Local
@Remote
public interface StatisticService {

	public StatisticEntity getStatistic(String statisticName,
			String applicationName) throws StatisticNotFoundException;

}
