package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;

@Local
@Remote
public interface StatisticService {

	public StatisticEntity getStatistic(String statisticName,
			String applicationName) throws StatisticNotFoundException,
			PermissionDeniedException;

	public List<StatisticEntity> getStatistics(ApplicationEntity application)
			throws PermissionDeniedException;

}
