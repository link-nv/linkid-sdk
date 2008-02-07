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
 * <h2>{@link ScenarioExecution} - A data structure that holds the results of a
 * scenario execution.</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 21, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioExecution implements Serializable,
		Comparable<ScenarioExecution>, Cloneable {

	private static final long serialVersionUID = 1L;

	private Map<String, byte[][]> charts;
	private String hostname;
	private Integer id;
	private Double speed;
	private Long duration;
	private Integer workers;
	private Integer agents;
	private String scenarioName;
	private Date startTime;

	public ScenarioExecution(Integer id, String scenarioName, Integer agents,
			Integer workers, Date startTime, Long duration, String hostname,
			Double speed) {

		this.id = id;
		this.scenarioName = scenarioName;
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

	public Integer getId() {

		return this.id;
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

	public String getScenario() {

		return this.scenarioName;
	}

	public Date getStart() {

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

		return String.format("[%s (%s)] %sx%s: %s min @ %s #/s",
				formattedStartTime == null ? "N/A" : formattedStartTime,
				this.id == null ? "N/A" : this.id, this.agents == null ? "N/A"
						: this.agents, this.workers == null ? "N/A"
						: this.workers, this.duration == null ? "N/A"
						: this.duration / 60000, this.speed == null ? "N/A"
						: String.format("%.2f", this.speed));
	}

	/**
	 * <b>NOTE</b>: The clone will <b>NOT</b> contain no charts even if this
	 * instance does!
	 *
	 * {@inheritDoc}
	 */
	@Override
	public ScenarioExecution clone() {

		return new ScenarioExecution(this.id, this.scenarioName, this.agents,
				this.workers, this.startTime, this.duration, this.hostname,
				this.speed);
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

		return equals(this.startTime, other.startTime, "time")
				&& equals(this.scenarioName, other.scenarioName, "scenario")
				&& equals(this.hostname, other.hostname, "host")
				&& equals(this.duration, other.duration, "duration")
				&& equals(this.workers, other.workers, "workers")
				&& equals(this.agents, other.agents, "agents")
				&& equals(this.speed, other.speed, "speed");
	}

	/**
	 * @return <code>true</code> if o1 and o2 are equal (<code>null</code-safe).
	 */
	private boolean equals(Object o1, Object o2, String desc) {

		if (o1 != null) {
			if (o2 == null || !o1.equals(o2)) {
				System.err.println(desc + ": " + o1 + " != " + o2);
				return false;
			}
		}

		else if (o2 != null) {
			System.err.println(desc + ": " + o1 + " != " + o2);
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(ScenarioExecution o) {

		if (this.startTime == null || o.startTime == null)
			return this.id.compareTo(o.id);

		return this.startTime.compareTo(o.startTime);
	}
}
