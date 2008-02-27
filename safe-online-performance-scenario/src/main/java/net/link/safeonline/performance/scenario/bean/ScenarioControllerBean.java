/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.scenario.ExecutionMetadata;
import net.link.safeonline.performance.scenario.Scenario;
import net.link.safeonline.performance.scenario.ScenarioController;
import net.link.safeonline.performance.scenario.charts.Chart;
import net.link.safeonline.performance.scenario.script.RegisteredScripts;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.performance.service.ProfileDataService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.TransactionTimeout;

/**
 * <h2>{@link ScenarioControllerBean}<br>
 * <sub>This bean is the heart of the scenario application.</sub></h2>
 *
 * <p>
 * We take care of preparing scenario execution and launching a single scenario
 * run. As these methods are called, entity objects are updated with state that
 * can later be used to graph out the progress of the scenario execution.<br>
 * <br>
 * Charts are also generated in this bean.<br>
 * Currently, we create charts for:
 * <ul>
 * <li>The time spent in the OLAS performance filter.</li>
 * <li>A component-based overview of where all the time needed to execute a
 * scenario was spent.</li>
 * <li>A component-based overview of where all the time needed to execute a
 * driver request was spent.</li>
 * <li>Evolution of available memory on the agent.</li>
 * <li>Evolution of available memory on OLAS.</li>
 * <li>A box-and-whisker chart comparing driver performance.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Stateless
@Local(ScenarioController.class)
@LocalBinding(jndiBinding = ScenarioController.BINDING)
public class ScenarioControllerBean implements ScenarioController {

	/*
	 * Timeout values for long running methods (in seconds).
	 */
	private static final int CHARTING_TIMEOUT = 5 * 60 * 60; // 5h
	private static final int SCENARIO_EXECUTION_TIMEOUT = 10 * 60; // 10m

	private static final Log LOG = LogFactory
			.getLog(ScenarioControllerBean.class);
	private static final int DATA_POINTS = 1000;

	private static MBeanServerConnection rmi;

	static {
		try {
			rmi = (MBeanServerConnection) getInitialContext().lookup(
					"jmx/invoker/RMIAdaptor");
		} catch (NamingException e) {
			LOG.error("JMX unavailable.", e);
		}
	}

	@EJB
	private ExecutionService executionService;

	@EJB
	private ProfileDataService profileDataService;

	@Resource
	SessionContext ctx;

	/**
	 * {@inheritDoc}
	 */
	@TransactionTimeout(SCENARIO_EXECUTION_TIMEOUT)
	public void execute(Date startTime) throws Exception {

		ExecutionEntity execution = this.executionService
				.getExecution(startTime);
		ScenarioTimingEntity agentTime = this.executionService.start(execution);
		agentTime.setStartMemory(getFreeMemory());

		Scenario scenario = createScenario(execution.getScenarioName());
		scenario.prepare(execution, agentTime);

		try {
			scenario.run();
		} finally {
			agentTime.stop();
			agentTime.setEndMemory(getFreeMemory());

			execution.dirtySpeed();
		}
	}

	/**
	 * Create an instance of the given scenario.
	 */
	private Scenario createScenario(String scenario) {

		try {
			return (Scenario) Thread.currentThread().getContextClassLoader()
					.loadClass(scenario).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Configured scenario '" + scenario
					+ "' cannot be created.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Date prepare(ExecutionMetadata metaData) {

		// Create the execution and fill it up with metadata.
		ExecutionEntity execution = this.executionService.addExecution(metaData
				.getScenarioName(), metaData.getAgents(),
				metaData.getWorkers(), metaData.getStartTime(), metaData
						.getDuration(), metaData.getHostname());
		createScenario(execution.getScenarioName()).prepare(execution, null);

		return execution.getStartTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getScenarios() {

		Set<String> scenarios = new HashSet<String>();
		for (Class<? extends Scenario> scenario : RegisteredScripts
				.getRegisteredScenarios())
			scenarios.add(scenario.getName());

		return scenarios;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Date> getExecutions() {

		return this.executionService.getExecutions();
	}

	/**
	 * {@inheritDoc}
	 */
	public ExecutionMetadata getExecutionMetadata(Date startTime) {

		ExecutionEntity execution = this.executionService
				.getExecution(startTime);

		return ExecutionMetadata.createResponse(execution.getScenarioName(),
				getDescription(execution.getScenarioName()), execution
						.getAgents(), execution.getWorkers(), execution
						.getStartTime(), execution.getDuration(), execution
						.getHostname(), execution.getSpeed());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription(String scenario) {

		return createScenario(scenario).getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionTimeout(CHARTING_TIMEOUT)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Map<String, byte[][]> createCharts(Date executionStartTime) {

		ExecutionEntity execution = this.executionService
				.getExecution(executionStartTime);

		List<Chart> charts = createScenario(execution.getScenarioName())
				.getCharts();

		Set<DriverProfileEntity> profiles = execution.getProfiles();
		for (DriverProfileEntity profile : profiles) {
			Set<ProfileDataEntity> profileData = this.profileDataService
					.getProfileData(profile, DATA_POINTS);
			for (ProfileDataEntity data : profileData)
				for (Chart chart : charts)
					try {
						chart.process(profile, data);
					} catch (Exception e) {
						LOG.error("While charting:", e);
					}
		}

		Map<String, byte[][]> images = new LinkedHashMap<String, byte[][]>();
		for (Chart chart : charts)
			images.put(chart.getTitle(), chart.render(DATA_POINTS));

		return images;
	}

	private long getFreeMemory() {

		try {
			return (Long) rmi.getAttribute(new ObjectName(
					"jboss.system:type=ServerInfo"), "FreeMemory");
		} catch (Exception e) {
			LOG.error("Failed to read in free memory through JMX.", e);
		}

		return -1;
	}

	private static InitialContext getInitialContext() throws NamingException {

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "localhost:1099");

		return new InitialContext(environment);
	}
}
