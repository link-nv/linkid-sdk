package net.link.safeonline.model.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.Task;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.model.ConfigurationManager;

import static net.link.safeonline.model.bean.UsageStatisticConfigurationProviderBean.activeLimitInMillis;
import static net.link.safeonline.model.bean.UsageStatisticConfigurationProviderBean.ageInMillis;

@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/" + "UsageStatisticTaskBean")
public class UsageStatisticTaskBean implements Task {

	public static final String name = "Usage statistic task";

	public static final String statisticName = "Usage statistic";

	public static final String loginCounter = "Login counter";

	@EJB
	private StatisticDAO statisticDAO;

	@EJB
	private StatisticDataPointDAO statisticDataPointDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private ConfigurationManager configurationManager;

	public String getName() {
		return UsageStatisticTaskBean.name;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void perform() {
		List<ApplicationEntity> applicationList = this.applicationDAO
				.listApplications();
		long activeLimit = Long.parseLong(this.configurationManager
				.findConfigItem(activeLimitInMillis).getValue());
		long age = Long.parseLong(this.configurationManager.findConfigItem(
				ageInMillis).getValue());

		for (ApplicationEntity application : applicationList) {
			long totalSubscriptions = this.subscriptionDAO
					.getNumberOfSubscriptions(application);
			long activeSubscriptions = this.subscriptionDAO
					.getActiveNumberOfSubscriptions(application, activeLimit);

			StatisticEntity statistic = this.statisticDAO
					.findOrAddStatisticByNameAndApplication(statisticName,
							application);

			StatisticDataPointEntity loginCounterDP = this.statisticDataPointDAO
					.findOrAddStatisticDataPoint(loginCounter, statistic);

			this.statisticDataPointDAO.addStatisticDataPoint(statisticName,
					statistic, totalSubscriptions, activeSubscriptions,
					loginCounterDP.getX());
			this.statisticDataPointDAO.cleanStatisticDataPoints(statistic, age);
			loginCounterDP.setX(0);

		}
	}

}
