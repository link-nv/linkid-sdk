/**
 * 
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Map;

import net.link.safeonline.performance.console.ScenarioDeployer;
import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;

/**
 * Threads that extend this class are used for delegating actions that should be
 * performed on {@link Agent}s and can take a long time in order to prevent
 * hanging the UI during this operation.
 * 
 * @author mbillemo
 */
public abstract class ScenarioThread extends Thread {

	private static Log LOG = LogFactory.getLog(ScenarioThread.class);

	Map<Address, Agent> agents;
	ScenarioDeployer scenarioDeployer;
	ScenarioChooser chooser;

	public ScenarioThread(Map<Address, Agent> agents, ScenarioChooser chooser) {

		this.agents = agents;
		this.chooser = chooser;
		this.scenarioDeployer = new ScenarioDeployer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		try {
			this.chooser.setButtonsEnabled(false);

			for (Map.Entry<Address, Agent> agentEntry : this.agents.entrySet()) {
				Agent agent = agentEntry.getValue();

				try {
					agent.setError(null);
					if (agent.isSelected())
						process(agentEntry.getKey(), agent);
				}

				catch (Exception e) {
					agent.setError(e);
					LOG.error("Scenario Failed During Execution", e);
				}
			}
		}

		finally {
			done();

			this.chooser.setButtonsEnabled(true);
		}
	}

	/**
	 * Perform the action that needs to be performed on each selected agent.
	 */
	abstract void process(Address address, Agent agent) throws Exception;

	/**
	 * Code to execute after the task has been completed.
	 */
	abstract void done();
}
