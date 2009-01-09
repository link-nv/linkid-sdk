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
 * This class keeps and listens to the components that contain the location of the OLAS service.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ExecutionSettings implements FocusListener, ItemListener, AgentSelectionListener, AgentStatusListener {

    private static final long serialVersionUID = 1L;

    protected JTextField      hostname;
    protected JTextField      port;
    protected JTextField      workers;
    protected JTextField      duration;
    protected JComboBox       scenarioSelection;
    protected JToggleButton   useSsl;


    public ExecutionSettings() {

        hostname = new JTextField(ConsoleData.getHostname());
        port = new JTextField(String.valueOf(ConsoleData.getPort()));
        workers = new JTextField(String.valueOf(ConsoleData.getWorkers()));
        duration = new JTextField(String.valueOf(ConsoleData.getDuration() / (60 * 1000)));
        useSsl = new JToggleButton("Use SSL", ConsoleData.isSsl());
        scenarioSelection = new JComboBox();
        scenarioSelection.setEnabled(false);

        hostname.addFocusListener(this);
        port.addFocusListener(this);
        workers.addFocusListener(this);
        duration.addFocusListener(this);
        useSsl.addItemListener(this);
        scenarioSelection.addItemListener(this);

        hostname.setToolTipText("The IP address or DNS resolvable name of the host that is running OLAS.");
        port.setToolTipText("The port on which OLAS' Application Server is configured to serve SSL.");
        workers.setToolTipText("The amount of simultaneous threads the agents should use for firing scenarios at OLAS.");
        duration.setToolTipText("The amount of time to run the tests for (in milliseconds).");

        ConsoleData.addAgentStatusListener(this);
        ConsoleData.addAgentSelectionListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void itemStateChanged(ItemEvent e) {

        if (scenarioSelection.equals(e.getSource())) {
            String scenario = null;
            if (scenarioSelection.getSelectedItem() != null) {
                scenario = ((ItemRenderer<String>) scenarioSelection.getSelectedItem()).getItem();
            }

            ConsoleData.setScenarioName(scenario);
        }

        else if (useSsl.equals(e.getSource())) {
            ConsoleData.setSsl(useSsl.isSelected());
        }
    }

    /**
     * @{inheritDoc
     */
    public void focusLost(FocusEvent e) {

        if (hostname.equals(e.getSource())) {
            ConsoleData.setHostname(hostname.getText());
        } else if (port.equals(e.getSource())) {
            ConsoleData.setPort(Integer.parseInt(port.getText()));
        } else if (workers.equals(e.getSource())) {
            ConsoleData.setWorkers(Integer.parseInt(workers.getText()));
        } else if (duration.equals(e.getSource())) {
            try {
                ConsoleData.setDuration(Long.parseLong(duration.getText()) * 60 * 1000);
            } catch (NumberFormatException err) {
            }
        }
    }

    /**
     * @{inheritDoc
     */
    public void focusGained(FocusEvent e) {

        focusLost(e);
    }

    /**
     * {@inheritDoc}
     */
    public void statusChanged(ConsoleAgent agent) {

        update();
    }

    /**
     * {@inheritDoc}
     */
    public void agentsSelected(Set<ConsoleAgent> selectedAgents) {

        update();
    }

    private void update() {

        Set<ConsoleAgent> selectedAgents = ConsoleData.getSelectedAgents();
        if (selectedAgents == null || selectedAgents.isEmpty()) {
            setScenarios(new HashSet<String>());
            return;
        }

        // Find the intersection of all executions with the same start time
        // and all scenarios with the same name.
        SortedSet<String> commonScenarios = new TreeSet<String>();
        for (ConsoleAgent agent : selectedAgents) {
            Set<String> agentScenarios = agent.getScenarios();

            if (agentScenarios == null) {
                agentScenarios = new HashSet<String>();
            }

            if (commonScenarios.isEmpty()) {
                commonScenarios.addAll(agentScenarios);
            } else {
                Iterator<String> it = commonScenarios.iterator();
                while (it.hasNext())
                    if (!agentScenarios.contains(it.next())) {
                        it.remove();
                    }
            }
        }

        setScenarios(commonScenarios);
    }

    /**
     * Modify the content of the scenario selections box in the swing event thread.
     */
    private void setScenarios(final Set<String> scenarios) {

        final JComboBox box = scenarioSelection;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                Object selectedItem = box.getSelectedItem();
                box.removeAllItems();

                for (String scenario : scenarios) {
                    box.addItem(new ItemRenderer<String>(scenario) {

                        @Override
                        public String toString() {

                            return item.replaceFirst(".*\\.", "");
                        }
                    });
                }

                box.setSelectedItem(selectedItem);
                if (box.getSelectedItem() == null && !scenarios.isEmpty()) {
                    box.setSelectedIndex(0);
                }
                box.setEnabled(box.getModel().getSize() > 0);
            }
        });
    }
}
