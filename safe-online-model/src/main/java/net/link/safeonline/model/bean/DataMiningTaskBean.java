package net.link.safeonline.model.bean;

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.Task;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.model.Applications;

@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/" + "DataMiningTaskBean")
public class DataMiningTaskBean implements Task {

	public static final String name = "Data mining task";

	public static final String dataMiningDomain = "Data Mining Domain";

	@EJB
	private StatisticDAO statisticDAO;

	@EJB
	private StatisticDataPointDAO statisticDataPointDAO;

	@EJB
	private Applications applications;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	public String getName() {
		return name;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void perform() throws Exception {

		this.statisticDAO.cleanDomain(dataMiningDomain);

		for (ApplicationEntity application : this.applications
				.listApplications()) {

			for (ApplicationIdentityAttributeEntity attribute : this.applications
					.getCurrentApplicationIdentity(application)) {

				StatisticEntity statistic = this.statisticDAO
						.findOrAddStatisticByNameDomainAndApplication(attribute
								.getAttributeTypeName(), dataMiningDomain,
								application);

				Map<String, Long> result = this.attributeTypeDAO.categorize(
						application, attribute.getAttributeType());

				for (String key : result.keySet()) {
					StatisticDataPointEntity datapoint = this.statisticDataPointDAO
							.findOrAddStatisticDataPoint(key, statistic);
					datapoint.setX(result.get(key));
				}

			}
		}

	}
}
