/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.util.Map;

import net.link.safeonline.performance.console.ScenarioDeployer;

import org.jgroups.Address;

/**
 * @author mbillemo
 * 
 */
public abstract class ScenarioThread extends Thread {

	AgentsList agents;
	ScenarioDeployer scenarioDeployer;
	ScenarioChooser chooser;

	public ScenarioThread(AgentsList agents, ScenarioChooser chooser) {

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

			for (Map.Entry<Address, Agent> agentEntry : this.agents.getAgents()
					.entrySet()) {
				Agent agent = agentEntry.getValue();
				agent.setError(null);

				try {
					if (this.agents.isSelected(agent))
						process(agentEntry.getKey(), agent);
				}

				catch (Exception e) {
					agent.setError(e);
					this.agents.repaint();
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
	abstract void process(Address address, Agent agent);

	/**
	 * Code to execute after the task has been completed.
	 */
	abstract void done();
}
