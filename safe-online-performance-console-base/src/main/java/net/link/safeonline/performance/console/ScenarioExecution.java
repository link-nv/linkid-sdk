/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <h2>{@link ScenarioExecution}<br>
 * <sub>A data structure that holds the results of a scenario execution.</sub></h2>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioExecution implements Serializable,
		Comparable<ScenarioExecution>, Cloneable {

	private static final long serialVersionUID = 1L;

	private Map<String, byte[][]> charts;
	private String hostname;
	private Double speed;
	private Long duration;
	private Integer workers;
	private Integer agents;
	private String scenarioName;
	private String scenarioDescription;
	private Date startTime;

	public ScenarioExecution(String scenarioName, String scenarioDescription,
			Integer agents, Integer workers, Date startTime, Long duration,
			String hostname, Double speed) {

		this.scenarioName = scenarioName;
		this.scenarioDescription = scenarioDescription;
		this.agents = agents;
		this.workers = workers;
		this.startTime = startTime;
		this.duration = duration;
		this.hostname = hostname;
		this.speed = speed;
	}

	public Map<String, byte[][]> getCharts() {

		return this.charts;
	}

	public void setCharts(Map<String, byte[][]> charts) {

		this.charts = charts;
	}

	public String getHostname() {

		return this.hostname;
	}

	public Double getSpeed() {

		return this.speed;
	}

	public Long getDuration() {

		return this.duration;
	}

	public Integer getWorkers() {

		return this.workers;
	}

	public Integer getAgents() {

		return this.agents;
	}

	public String getScenarioName() {

		return this.scenarioName;
	}

	public String getScenarioDescription() {

		return this.scenarioDescription;
	}

	public Date getStartTime() {

		return this.startTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		String formattedStartTime = null;
		if (this.startTime != null)
			formattedStartTime = new SimpleDateFormat("HH:mm")
					.format(this.startTime);

		return String.format("%s: [%s] %sx%s (%s min): %s #/s",
				this.scenarioName == null ? "N/A" : this.scenarioName
						.replaceFirst(".*\\.", ""),
				formattedStartTime == null ? "N/A" : formattedStartTime,
				this.agents == null ? "N/A" : this.agents,
				this.workers == null ? "N/A" : this.workers,
				this.duration == null ? "N/A" : this.duration / 60000,
				this.speed == null ? "N/A" : String.format("%.2f", this.speed));
	}

	/**
	 * <b>NOTE</b>: The clone will <b>NOT</b> contain no charts even if this
	 * instance does!
	 *
	 * {@inheritDoc}
	 */
	@Override
	public ScenarioExecution clone() {

		return new ScenarioExecution(this.scenarioName,
				this.scenarioDescription, this.agents, this.workers,
				this.startTime, this.duration, this.hostname, this.speed);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof ScenarioExecution))
			return false;
		if (obj == this)
			return true;

		ScenarioExecution other = (ScenarioExecution) obj;

		return equals(this.startTime, other.startTime)
				&& equals(this.scenarioName, other.scenarioName)
				&& equals(this.hostname, other.hostname)
				&& equals(this.duration, other.duration)
				&& equals(this.workers, other.workers)
				&& equals(this.agents, other.agents)
				&& equals(this.speed, other.speed);
	}

	/**
	 * @return <code>true</code> if o1 and o2 are equal (<code>null</code>-safe).
	 */
	private boolean equals(Object o1, Object o2) {

		if (o1 != null) {
			if (o2 == null || !o1.equals(o2))
				return false;
		}

		else if (o2 != null)
			return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(ScenarioExecution o) {

		return this.startTime.compareTo(o.startTime);
	}
}
