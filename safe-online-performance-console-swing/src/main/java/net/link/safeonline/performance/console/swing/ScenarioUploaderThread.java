/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.io.File;

import org.jgroups.Address;

/**
 * @author mbillemo
 * 
 */
public class ScenarioUploaderThread extends ScenarioThread {

	File application;

	public ScenarioUploaderThread(AgentsList agents, ScenarioChooser chooser,
			File application) {

		super(agents, chooser);
		this.application = application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(Address address, Agent agent) {

		try {
			agent.setUploading(true);
			this.agents.repaint();

			this.scenarioDeployer.upload(address, this.application);
		}

		finally {
			agent.setUploading(false);
			this.agents.repaint();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void done() {

		this.chooser.setReadyForDeployment(true);
	}
}
