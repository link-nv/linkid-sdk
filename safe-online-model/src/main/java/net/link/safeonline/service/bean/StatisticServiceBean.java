/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.service.ApplicationOwnerAccessControlInterceptor;
import net.link.safeonline.service.StatisticService;
import net.link.safeonline.service.StatisticServiceRemote;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class StatisticServiceBean implements StatisticService,
		StatisticServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(StatisticServiceBean.class);

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private StatisticDAO statisticDAO;

	@RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
	public StatisticEntity getStatistic(String statisticName,
			String statisticDomain, String applicationName)
			throws StatisticNotFoundException, PermissionDeniedException {

		ApplicationEntity application = null;
		if (applicationName != null) {
			LOG.debug("finding application");
			application = this.applicationDAO.findApplication(applicationName);
		}

		return this.getStatistic(statisticName, statisticDomain, application);
	}

	@Interceptors(ApplicationOwnerAccessControlInterceptor.class)
	private StatisticEntity getStatistic(String statisticName,
			String statisticDomain, ApplicationEntity application)
			throws StatisticNotFoundException {
		LOG.debug("finding statistic");
		StatisticEntity statistic = this.statisticDAO
				.findStatisticByNameDomainAndApplication(statisticName,
						statisticDomain, application);
		if (statistic == null) {
			throw new StatisticNotFoundException();
		}

		// trigger fetching
		LOG.debug("fetching datapoints");
		for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
			dp.getId();
		}
		return statistic;
	}

	@RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
	@Interceptors(ApplicationOwnerAccessControlInterceptor.class)
	public List<StatisticEntity> getStatistics(ApplicationEntity application)
			throws PermissionDeniedException {
		List<StatisticEntity> result = this.statisticDAO
				.listStatistics(application);
		return result;
	}

}
