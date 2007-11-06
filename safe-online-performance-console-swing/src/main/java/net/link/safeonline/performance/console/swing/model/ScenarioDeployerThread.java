/**
 * 
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Map;

import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser.DeploymentPhase;

import org.jgroups.Address;

/**
 * This thread deploys a scenario on a given agent and manages the {@link Agent}
 * object's deployment status.
 * 
 * @author mbillemo
 */
public class ScenarioDeployerThread extends ScenarioThread {

	public ScenarioDeployerThread(Map<Address, Agent> map,
			ScenarioChooser chooser) {

		super(map, chooser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(Address address, Agent agent) throws Exception {

		try {
			if (!agent.setDeploying(true))
				return;

			this.scenarioDeployer.deploy(address);
		}

		finally {
			agent.setDeploying(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void done() {

		this.chooser.setDeploymentPhase(DeploymentPhase.EXECUTE);
	}
}
