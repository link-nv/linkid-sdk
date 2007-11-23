/**
 * 
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Map;

import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.data.Agent.State;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

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

		super(State.DEPLOY, map, chooser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(Address address, Agent agent) throws Exception {

		this.scenarioDeployer.deploy(address);
	}
}
