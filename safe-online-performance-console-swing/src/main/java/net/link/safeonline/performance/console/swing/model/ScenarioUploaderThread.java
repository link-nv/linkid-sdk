/**
 * 
 */
package net.link.safeonline.performance.console.swing.model;

import java.io.File;
import java.util.Map;

import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser.DeploymentPhase;

import org.jgroups.Address;

/**
 * This thread uploads a scenario to a given agent and manages the {@link Agent}
 * object's uploading status.
 * 
 * @author mbillemo
 */
public class ScenarioUploaderThread extends ScenarioThread {

	File application;

	public ScenarioUploaderThread(Map<Address, Agent> map,
			ScenarioChooser chooser, File application) {

		super(map, chooser);
		this.application = application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(Address address, Agent agent) throws Exception {

		try {
			if (!agent.setUploading(true))
				return;

			this.scenarioDeployer.upload(address, this.application);
		}

		finally {
			agent.setUploading(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void done() {

		this.chooser.setDeploymentPhase(DeploymentPhase.DEPLOY);
	}
}
