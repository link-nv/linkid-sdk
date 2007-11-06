/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import net.link.safeonline.performance.drivers.ProfileDriver;
import net.link.safeonline.performance.scenario.ScenarioRemote;
import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Millisecond;

/**
 * @author mbillemo
 * 
 */
@Stateless
@RemoteBinding(jndiBinding = "SafeOnline/ScenarioBean")
public class ScenarioBean implements ScenarioRemote {

	private static final Log LOG = LogFactory.getLog(ScenarioBean.class);

	private List<ProfileDriver> drivers;

	public List<JFreeChart> execute(String hostname) {

		Scenario scenario = new BasicScenario();
		this.drivers = scenario.prepare(hostname);

		// Execute the scenario story.
		for (int iteration = 0; iteration < scenario.getIterations(); ++iteration)
			try {
				scenario.execute();
			} catch (Exception e) {
				LOG.error("Test " + iteration + " failed.", e);
			}

		// Generate the resulting statistical information.
		return createGraphs();
	}

	protected void register(ProfileDriver... profileDrivers) {

		this.drivers.addAll(Arrays.asList(profileDrivers));
	}

	/**
	 * @return the graphs of the statistics collected during the execution of
	 *         this scenario.
	 */
	private List<JFreeChart> createGraphs() {

		List<JFreeChart> charts = new ArrayList<JFreeChart>();
		for (ProfileDriver driver : this.drivers) {

			// Data.
			long maxTime = 0;
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (int i = 0; i < driver.getProfileData().size(); ++i) {

				ProfileData data = driver.getProfileData().get(i);

				// Each Method Call.
				for (Map.Entry<String, Long> measurement : data
						.getMeasurements().entrySet()) {
					String method = measurement.getKey();
					Long timing = measurement.getValue();

					if (!"127.0.0.1".equals(method))
						dataset.addValue(timing, method, "Test " + i);
				}

				// Add Total at the End.
				long total = data.getMeasurements().get("127.0.0.1");
				dataset.addValue(total, "Request Time", "Total " + i);
				maxTime = Math.max(total, maxTime);
			}

			// Legend & Scale.
			CategoryAxis catAxis = new CategoryAxis("Iterations");
			ValueAxis valueAxis = new PeriodAxis("Time Elapsed",
					new Millisecond(new Date(0)), new Millisecond(new Date(
							maxTime)));

			// Chart.
			BarRenderer renderer = new StackedBarRenderer();
			CategoryPlot plot = new CategoryPlot(dataset, catAxis, valueAxis,
					renderer);
			JFreeChart chart = new JFreeChart(driver.getTitle(), plot);

			// Image.
			charts.add(chart);
		}

		return charts;
	}
}
