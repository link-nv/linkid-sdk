/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import org.jgroups.Address;

/**
 * @author mbillemo
 * 
 */
public class ScenarioDeployerThread extends ScenarioThread {

	public ScenarioDeployerThread(AgentsList agents, ScenarioChooser chooser) {

		super(agents, chooser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(Address address, Agent agent) {

		try {
			agent.setDeploying(true);
			this.agents.repaint();

			this.scenarioDeployer.deploy(address);
		}

		finally {
			agent.setDeploying(false);
			this.agents.repaint();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void done() {

		this.chooser.setReadyForDeployment(false);
	}
}
