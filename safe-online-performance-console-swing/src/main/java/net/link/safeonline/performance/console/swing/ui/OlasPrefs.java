/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.model.AgentSelectionListener;

/**
 * This class keeps and listens to the components that contain the location of
 * the OLAS service.
 *
 * @author mbillemo
 */
public class OlasPrefs implements FocusListener, ItemListener,
		AgentSelectionListener {

	private static final long serialVersionUID = 1L;

	protected JTextField hostname;
	protected JTextField port;
	protected JTextField workers;
	protected JTextField duration;
	protected JComboBox scenarioSelection;

	public OlasPrefs() {

		this.hostname = new JTextField(ConsoleData.getHostname());
		this.port = new JTextField(String.valueOf(ConsoleData.getPort()));
		this.workers = new JTextField(String.valueOf(ConsoleData.getWorkers()));
		this.duration = new JTextField(String
				.valueOf(ConsoleData.getDuration()));
		this.scenarioSelection = new JComboBox();
		this.scenarioSelection.setEnabled(false);

		this.hostname.addFocusListener(this);
		this.port.addFocusListener(this);
		this.workers.addFocusListener(this);
		this.duration.addFocusListener(this);
		this.scenarioSelection.addItemListener(this);

		this.hostname
				.setToolTipText("The IP address or DNS resolvable name of the host that is running OLAS.");
		this.port
				.setToolTipText("The port on which OLAS' Application Server is configured to serve SSL.");
		this.workers
				.setToolTipText("The amount of simultaneous threads the agents should use for firing scenarios at OLAS.");
		this.duration
				.setToolTipText("The amount of time to run the tests for (in milliseconds).");

		ConsoleData.addAgentSelectionListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void itemStateChanged(ItemEvent e) {

		if (this.scenarioSelection.equals(e.getSource()))
			if (this.scenarioSelection.getSelectedItem() == null)
				ConsoleData.setScenarioName(null);
			else
				ConsoleData.setScenarioName(this.scenarioSelection
						.getSelectedItem().toString());
	}

	/**
	 * @{inheritDoc}
	 */
	public void focusLost(FocusEvent e) {

		if (this.hostname.equals(e.getSource()))
			ConsoleData.setHostname(this.hostname.getText());

		else if (this.port.equals(e.getSource()))
			ConsoleData.setPort(Integer.parseInt(this.port.getText()));

		else if (this.workers.equals(e.getSource()))
			ConsoleData.setWorkers(Integer.parseInt(this.workers.getText()));

		else if (this.duration.equals(e.getSource()))
			try {
				ConsoleData
						.setDuration(Long.parseLong(this.duration.getText()));
			} catch (NumberFormatException err) {
			}
	}

	/**
	 * @{inheritDoc}
	 */
	public void focusGained(FocusEvent e) {

		focusLost(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentsSelected(Set<ConsoleAgent> selectedAgents) {

		if (selectedAgents == null || selectedAgents.isEmpty()) {
			this.scenarioSelection.removeAllItems();
			this.scenarioSelection.setEnabled(false);
			return;
		}

		// Find the intersection of all executions with the same start time
		// and all scenarios with the same name.
		SortedSet<String> commonScenarios = new TreeSet<String>();
		for (ConsoleAgent agent : selectedAgents) {
			Set<String> agentScenarios = agent.getScenarios();

			if (agentScenarios == null)
				agentScenarios = new HashSet<String>();

			if (commonScenarios.isEmpty())
				commonScenarios.addAll(agentScenarios);
			else {
				Iterator<String> it = commonScenarios.iterator();
				while (it.hasNext())
					if (!agentScenarios.contains(it.next()))
						it.remove();
			}
		}

		Object selectedItem = this.scenarioSelection.getSelectedItem();
		this.scenarioSelection.removeAllItems();
		for (String scenario : commonScenarios)
			this.scenarioSelection.addItem(scenario);
		this.scenarioSelection.setSelectedItem(selectedItem);
		this.scenarioSelection.setEnabled(this.scenarioSelection.getModel()
				.getSize() > 0);
	}
}
