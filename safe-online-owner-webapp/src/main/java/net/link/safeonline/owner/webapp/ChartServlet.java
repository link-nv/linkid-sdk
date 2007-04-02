/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner.webapp;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.service.StatisticService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class ChartServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(ChartServlet.class);

	private static final String usageStatistic = "Usage statistic";

	private StatisticService statisticService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		loadDependencies();
	}

	private void loadDependencies() {
		this.statisticService = EjbUtils
				.getEJB("SafeOnline/StatisticServiceBean/local",
						StatisticService.class);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("image/png");
		OutputStream out = response.getOutputStream();

		String chartName = request.getParameter("chartname");
		String applicationName = request.getParameter("applicationname");

		if (null == chartName) {
			throw new ServletException("chartname request parameter missing");
		}
		if (null == applicationName) {
			throw new ServletException(
					"aplicationname request parameter missing");
		}

		LOG.debug("getting chart: " + chartName + " for application: "
				+ applicationName);

		byte[] buffer = null;
		try {
			buffer = getChart(chartName, applicationName, 800, 600);
		} catch (Exception e) {
			LOG.debug("exception: " + e.getMessage());
			throw new ServletException(e.getMessage(), e);
		}
		if (buffer != null) {
			out.write(buffer);
		}

	}

	public byte[] getChart(String chartName, String applicationName, int width,
			int heigth) throws StatisticNotFoundException,
			PermissionDeniedException {
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
