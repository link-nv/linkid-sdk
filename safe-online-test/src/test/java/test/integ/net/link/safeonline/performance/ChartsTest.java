/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package test.integ.net.link.safeonline.performance;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.scenario.Scenario;
import net.link.safeonline.performance.scenario.charts.Chart;

import org.junit.Test;

/**
 * <h2>{@link ChartsTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Mar 3, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ChartsTest extends AbstractDataTest {

	private static final int DATA_POINTS = 800;

	@Test
	@SuppressWarnings("unchecked")
	public void chartTest() throws Exception {

		// Get the most recent execution.
		Date executionId = new TreeSet<Date>(this.executionService
				.getExecutions()).last();
		ExecutionEntity execution = this.executionService
				.getExecution(executionId);

		// Create the scenario that was used.
		String scenarioName = execution.getScenarioName();
		Scenario scenario = (Scenario) Thread.currentThread()
				.getContextClassLoader().loadClass(scenarioName).newInstance();

		// Generate charts for this execution.
		List<Chart> charts = scenario.getCharts();
		List<ScenarioTimingEntity> scenarioTimings = this.scenarioTimingService
				.getExecutionTimings(execution);
		int i = 0;
		for (ScenarioTimingEntity timing : scenarioTimings) {
			for (Chart chart : charts)
				try {
					chart.processTiming(timing);
				} catch (Exception e) {
					this.LOG.error("Charting Timing Failed:", e);
				}
			if (++i > 100)
				return;
		}

		Set<DriverProfileEntity> profiles = execution.getProfiles();
		for (DriverProfileEntity profile : profiles) {
			List<ProfileDataEntity> profileData = this.profileDataService
					.getProfileData(profile, DATA_POINTS);
			for (ProfileDataEntity data : profileData)
				for (Chart chart : charts)
					try {
						chart.processData(data);
					} catch (Exception e) {
						this.LOG.error("Charting Data Failed:", e);
					}
		}

		Map<String, byte[][]> images = new LinkedHashMap<String, byte[][]>();
		for (Chart chart : charts)
			images.put(chart.getTitle(), chart.render(DATA_POINTS));

		// Display these charts.
		JDialog dialog = new JDialog();
		JPanel contentPane = new JPanel();
		dialog.setContentPane(new JScrollPane(contentPane));

		for (Map.Entry<String, byte[][]> chart : images.entrySet())
			contentPane.add(new JLabel(chart.getKey(), new ImageIcon(chart
					.getValue()[0]), SwingConstants.CENTER));

		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}
