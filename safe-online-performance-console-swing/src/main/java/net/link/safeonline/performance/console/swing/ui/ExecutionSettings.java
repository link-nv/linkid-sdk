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
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.model.AgentSelectionListener;

/**
 * <h2>{@link ExecutionSettings}<br>
 * <sub>Manage execution preferences.</sub></h2>
 *
 * <p>
 * This class keeps and listens to the components that contain the location of
 * the OLAS service.
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ExecutionSettings implements FocusListener, ItemListener,
		AgentSelectionListener {

	private static final long serialVersionUID = 1L;

	protected JTextField hostname;
	protected JTextField port;
	protected JTextField workers;
	protected JTextField duration;
	protected JComboBox scenarioSelection;
	protected JToggleButton useSsl;

	public ExecutionSettings() {

		this.hostname = new JTextField(ConsoleData.getHostname());
		this.port = new JTextField(String.valueOf(ConsoleData.getPort()));
		this.workers = new JTextField(String.valueOf(ConsoleData.getWorkers()));
		this.duration = new JTextField(String.valueOf(ConsoleData.getDuration()
				/ (60 * 1000)));
		this.useSsl = new JToggleButton("Use SSL", ConsoleData.isSsl());
		this.scenarioSelection = new JComboBox();
		this.scenarioSelection.setEnabled(false);

		this.hostname.addFocusListener(this);
		this.port.addFocusListener(this);
		this.workers.addFocusListener(this);
		this.duration.addFocusListener(this);
		this.useSsl.addItemListener(this);
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
	@SuppressWarnings("unchecked")
	public void itemStateChanged(ItemEvent e) {

		if (this.scenarioSelection.equals(e.getSource())) {
			String scenario = null;
			if (this.scenarioSelection.getSelectedItem() != null)
				scenario = ((ItemRenderer<String>) this.scenarioSelection
						.getSelectedItem()).getItem();

			ConsoleData.setScenarioName(scenario);
		}
		
		else if (this.useSsl.equals(e.getSource()))
			ConsoleData.setSsl(this.useSsl.isSelected());
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
						.setDuration(Long.parseLong(this.duration.getText()) * 60 * 1000);
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
			setScenarios(new HashSet<String>());
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

		setScenarios(commonScenarios);
	}

	/**
	 * Modify the content of the scenario selections box in the swing event
	 * thread.
	 */
	private void setScenarios(final Set<String> scenarios) {

		final JComboBox box = this.scenarioSelection;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				Object selectedItem = box.getSelectedItem();
				box.removeAllItems();

				for (String scenario : scenarios)
					box.addItem(new ItemRenderer<String>(scenario) {
						@Override
						public String toString() {

							return this.item.replaceFirst(".*\\.", "");
						}
					});

				box.setSelectedItem(selectedItem);
				if (box.getSelectedItem() == null && !scenarios.isEmpty())
					box.setSelectedIndex(0);
				box.setEnabled(box.getModel().getSize() > 0);
			}
		});
	}
}
