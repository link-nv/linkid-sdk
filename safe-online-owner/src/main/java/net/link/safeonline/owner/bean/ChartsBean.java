package net.link.safeonline.owner.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.owner.Charts;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.service.StatisticService;

@Stateful
@Name("chart")
@LocalBinding(jndiBinding = OwnerConstants.JNDI_PREFIX + "ChartsBean/local")
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
public class ChartsBean implements Charts {

	private static final Log LOG = LogFactory.getLog(ChartsBean.class);

	private static final String usageStatistic = "Usage statistic";

	@EJB
	private StatisticService statisticService;

	@In(create = true)
	FacesMessages facesMessages;

	@In(value = "selectedApplication", required = false)
	@Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
	private ApplicationEntity selectedApplication;

	@SuppressWarnings("unused")
	@Out(value = "chartURL", required = false, scope = ScopeType.SESSION)
	private String chartURL;

	@SuppressWarnings("unused")
	@DataModel
	private List<StatisticEntity> statList;

	@DataModelSelection("statList")
	@Out(value = "selectedStat", required = false, scope = ScopeType.SESSION)
	private StatisticEntity selectedStat;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@Factory("statList")
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public void statListFactory() throws PermissionDeniedException {
		LOG.debug("selectedApplication: " + this.selectedApplication);
		this.statList = this.statisticService
				.getStatistics(selectedApplication);
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String viewStat() {
		this.chartURL = "view.chart?chartname=" + selectedStat.getName()
				+ "&applicationname=" + selectedApplication.getName();
		return "viewstat";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public byte[] getChart(String chartName, String applicationName, int width,
			int heigth) throws StatisticNotFoundException,
			PermissionDeniedException {
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
		}
		if (chartName.equals(usageStatistic)) {
			chart = usageChart(statistic);
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

	private JFreeChart usageChart(StatisticEntity statistic) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries numberOfUsers = new XYSeries("Number of Users");
		XYSeries numberOfActiveUsers = new XYSeries("Number of Active Users");
		XYSeries numberOfLogins = new XYSeries("Number of Logins");

		for (StatisticDataPointEntity dp : statistic.getStatisticDataPoints()) {
			if (dp.getName().equals(usageStatistic)) {
				numberOfUsers.add(dp.getCreationTime().getTime(), dp.getX());
				numberOfActiveUsers.add(dp.getCreationTime().getTime(), dp
						.getY());
				numberOfLogins.add(dp.getCreationTime().getTime(), dp.getZ());
			}
		}

		dataset.addSeries(numberOfUsers);
		dataset.addSeries(numberOfActiveUsers);
		dataset.addSeries(numberOfLogins);

		JFreeChart chart = ChartFactory.createXYLineChart(statistic.getName(),
				"Time", "Number", dataset, PlotOrientation.VERTICAL, true,
				true, false);

		chart.getXYPlot().setDomainAxis(new DateAxis());
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesShapesVisible(0, true);
		renderer.setSeriesShapesVisible(1, true);
		renderer.setSeriesShapesVisible(2, true);
		chart.getXYPlot().setRenderer(renderer);
		chart.getXYPlot().getRangeAxis().setStandardTickUnits(
				NumberAxis.createIntegerTickUnits());

		return chart;
	}
}
