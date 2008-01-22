/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console;

import java.io.Serializable;
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
public class ScenarioExecution implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, byte[][]> charts;
	private String hostname;
	private Integer execution;
	private Long duration;
	private Integer workers;
	private Integer agents;

	public ScenarioExecution(Integer agents, Integer workers, Long duration,
			Integer execution, String hostname, Map<String, byte[][]> charts) {

		this.agents = agents;
		this.workers = workers;
		this.duration = duration;
		this.execution = execution;
		this.hostname = hostname;
		this.charts = charts;
	}

	public Map<String, byte[][]> getCharts() {

		return this.charts;
	}

	public String getHostname() {

		return this.hostname;
	}

	public Integer getExecution() {

		return this.execution;
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

}
