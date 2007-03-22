package net.link.safeonline.owner.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Name;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.owner.Charts;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.service.StatisticService;

@Stateless
@Name("charts")
@LocalBinding(jndiBinding = OwnerConstants.JNDI_PREFIX + "ChartsBean/local")
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
public class ChartsBean implements Charts {

	private static final Log LOG = LogFactory.getLog(ChartsBean.class);

	@EJB
	private StatisticService statisticService;

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public byte[] getChart(String chartName, String applicationName, int width,
			int heigth) throws StatisticNotFoundException {
		// TODO Access control

		LOG.debug("finding statistic: " + chartName + " for application: "
				+ applicationName);
		StatisticEntity statistic = this.statisticService.getStatistic(
				chartName, applicationName);

		JFreeChart chart = null;

		LOG.debug("found statistic");
		// hook specific chart generation functions here
		if (chartName.equals("blah")) {
			chart = specificChart(statistic);
		} else {
			chart = defaultChart(statistic);
		}

		byte[] result = null;

		try {
			result = ChartUtilities.encodeAsPNG(chart.createBufferedImage(
					width, heigth));
		} catch (Exception e) {
			LOG.debug("Could not generate image");
			return null;
		}

		LOG.debug("returning byte[]");
		return result;
	}

	public JFreeChart defaultChart(StatisticEntity statistic) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
			dataset.addValue(dp.getX(), "X", dp.getName());
			dataset.addValue(dp.getY(), "Y", dp.getName());
			dataset.addValue(dp.getZ(), "Z", dp.getName());
		}

		JFreeChart chart = ChartFactory.createBarChart(statistic.getName(), // chart
				// title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		return chart;
	}

	private JFreeChart specificChart(StatisticEntity statistic) {
		return this.defaultChart(statistic);
	}

}
