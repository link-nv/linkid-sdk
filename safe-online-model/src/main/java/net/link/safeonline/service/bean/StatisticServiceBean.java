package net.link.safeonline.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.service.StatisticService;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class StatisticServiceBean implements StatisticService {

	private static final Log LOG = LogFactory
			.getLog(StatisticServiceBean.class);

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private StatisticDAO statisticDAO;

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public StatisticEntity getStatistic(String statisticName,
			String applicationName) throws StatisticNotFoundException {
		ApplicationEntity application = null;
		if (applicationName != null) {
			LOG.debug("finding application");
			application = this.applicationDAO.findApplication(applicationName);
		}

		LOG.debug("finding statistic");
		StatisticEntity statistic = this.statisticDAO
				.findStatisticByNameAndApplication(statisticName, application);
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

}
