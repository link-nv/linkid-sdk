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
package net.link.safeonline.performance.scenario.charts;

import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;

/**
 * <h2>{@link Chart}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Feb 22, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public interface Chart {

	/**
	 * The title of the chart whenever it is displayed to users.
	 */
	public String getTitle();

	/**
	 * Process one chunk of {@link ProfileDataEntity}s as and if required for
	 * the rendering of this chart.
	 */
	public void processData(ProfileDataEntity data);

	/**
	 * Process one chunk of {@link ScenarioTimingEntity}s as and if required
	 * for the rendering of this chart.
	 */
	public void processTiming(ScenarioTimingEntity data);

	/**
	 * Render this chart to an image from the previously processed data.
	 *
	 * @param dataPoints
	 *            The amount of pixels one chart image should be wide.
	 */
	public byte[][] render(int dataPoints);

}
