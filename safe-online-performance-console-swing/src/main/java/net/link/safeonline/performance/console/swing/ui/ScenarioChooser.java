/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.model.AgentRefreshThread;
import net.link.safeonline.performance.console.swing.model.AgentSelectionListener;
import net.link.safeonline.performance.console.swing.model.ExecutionSelectionListener;
import net.link.safeonline.performance.console.swing.model.ExecutionSettingsListener;
import net.link.safeonline.performance.console.swing.model.ScenarioCharterThread;
import net.link.safeonline.performance.console.swing.model.ScenarioDeployerThread;
import net.link.safeonline.performance.console.swing.model.ScenarioExecutorThread;
import net.link.safeonline.performance.console.swing.model.ScenarioUploaderThread;


/**
 * <h2>{@link ScenarioChooser}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * This class keeps and listens to the UI components that upload, deploy and execute scenarios on agents.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ScenarioChooser extends JPanel implements ActionListener, CaretListener, AgentSelectionListener,
        ExecutionSelectionListener, ExecutionSettingsListener, AgentStatusListener {

    private static final long serialVersionUID = 1L;

    private JButton           uploadButton;
    private JButton           deployButton;
    private JButton           executeButton;
    private JButton           chartsButton;
    private JButton           pdfButton;
    private JButton           refreshButton;
    private JButton           resetButton;
    protected JButton         browseButton;
    protected JPanel          actionButtons;
    protected JTextField      scenarioField;


    public ScenarioChooser() {

        this.scenarioField = new JTextField(
                "/Users/mbillemo/Documents/design/safe-online/safe-online-performance-scenario-deploy/target/safe-online-performance-scenario-deploy-1.0-SNAPSHOT.ear");
        this.browseButton = new JButton("Browse ...");
        this.uploadButton = new JButton("Upload this Scenario");
        this.deployButton = new JButton("Deploy this Scenario");
        this.executeButton = new JButton("Execute this Scenario");
        this.chartsButton = new JButton("View Charts");
        this.pdfButton = new JButton("Create PDF");
        this.refreshButton = new JButton("Refresh the Agent Status");
        this.resetButton = new JButton("Reset the Agent");

        JPanel firstRow = new JPanel(new GridLayout(1, 0, 10, 0));
        JPanel secondRow = new JPanel(new GridLayout(1, 0, 10, 0));
        this.actionButtons = new JPanel(new GridLayout(2, 1, 0, 5));
        this.actionButtons.add(firstRow);
        this.actionButtons.add(secondRow);

        firstRow.add(this.uploadButton);
        firstRow.add(this.deployButton);
        firstRow.add(this.executeButton);
        firstRow.add(this.chartsButton);
        firstRow.add(this.pdfButton);
        secondRow.add(this.refreshButton);
        secondRow.add(this.resetButton);

        this.scenarioField.addCaretListener(this);
        this.browseButton.addActionListener(this);
        this.uploadButton.addActionListener(this);
        this.deployButton.addActionListener(this);
        this.executeButton.addActionListener(this);
        this.chartsButton.addActionListener(this);
        this.pdfButton.addActionListener(this);
        this.refreshButton.addActionListener(this);
        this.resetButton.addActionListener(this);

        this.scenarioField
                .setToolTipText("The EAR package that contains the scenario that must be uploaded to and executed by the agent.");
        this.browseButton.setToolTipText("Browse your hard disk for the scenario package to use.");
        this.uploadButton
                .setToolTipText("Upload the given scenario to the selected agents.  Undeploys any existing scenarios from the agents.");
        this.deployButton
                .setToolTipText("Instruct the selected agents to deploy the last scenario that was uploaded to them.");
        this.executeButton
                .setToolTipText("Execute the scenario on the selected agents for the given duration on the given OLAS host.");
        this.chartsButton
                .setToolTipText("Display the charts generated by the selected agents from their previously completed profiling run.");
        this.pdfButton
                .setToolTipText("Create, save and open a PDF file generated from the statistics collected by the selected agents.");
        this.refreshButton.setToolTipText("Query the current remote status of the selected agents.");
        this.resetButton.setToolTipText("Temporarily reset the selected agents' local status to unlock the buttons.");

        agentsSelected(null);

        ConsoleData.addExecutionSelectionListener(this);
        ConsoleData.addExecutionSettingsListener(this);
        ConsoleData.addAgentSelectionListener(this);
        ConsoleData.addAgentStatusListener(this);
    }

    /**
     * @{inheritDoc
     */
    public void actionPerformed(ActionEvent e) {

        if (this.browseButton.equals(e.getSource())) {

            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {

                    return f.getName().endsWith(".ear") || f.isDirectory();
                }

                @Override
                public String getDescription() {

                    return "Enterprise Archive (*.ear)";
                }
            });

            if (chooser.showDialog(this, "Choose") == JFileChooser.APPROVE_OPTION)
                this.scenarioField.setText(chooser.getSelectedFile().getPath());
        }

        else if (this.uploadButton.equals(e.getSource()))
            new ScenarioUploaderThread(getScenarioFile()).start();

        else if (this.deployButton.equals(e.getSource()))
            new ScenarioDeployerThread().start();

        else if (this.executeButton.equals(e.getSource()))
            new ScenarioExecutorThread().start();

        else if (this.chartsButton.equals(e.getSource()))
            new ScenarioCharterThread(false).start();

        else if (this.pdfButton.equals(e.getSource()))
            new ScenarioCharterThread(true).start();

        else if (this.refreshButton.equals(e.getSource()))
            new AgentRefreshThread(false).start();

        else if (this.resetButton.equals(e.getSource()))
            new AgentRefreshThread(true).start();
    }

    /**
     * @{inheritDoc
     */
    public void caretUpdate(CaretEvent e) {

        if (e.getSource().equals(this.scenarioField))
            buttonToggler(null != getScenarioFile(), this.uploadButton);
    }

    /**
     * {@inheritDoc}
     */
    public void executionSelected(ScenarioExecution execution) {

        updateButtons();
    }

    /**
     * {@inheritDoc}
     */
    public void executionSettingsChanged() {

        updateButtons();
    }

    /**
     * {@inheritDoc}
     */
    public void agentsSelected(Set<ConsoleAgent> agents) {

        updateButtons();
    }

    /**
     * {@inheritDoc}
     */
    public void statusChanged(ConsoleAgent agent) {

        updateButtons();
    }

    private void updateButtons() {

        // System.err.println("----");
        // Don't enable any buttons when no agents are selected.
        if (ConsoleData.getSelectedAgents().isEmpty()) {
            // System.err.println("No agents selected.");
            buttonToggler(false, this.refreshButton, this.resetButton, this.uploadButton, this.deployButton,
                    this.executeButton, this.chartsButton, this.pdfButton);
            return;
        }

        // System.err
        // .println("scenario file is null? " + getScenarioFile() == null);
        // System.err.println("scenario name is null? "
        // + ConsoleData.getScenarioName() == null);
        // System.err.println("execution selected ? "
        // + ConsoleData.getSelectedExecution() != null);

        // Enable all buttons available with the current configuration.
        buttonToggler(true, this.deployButton);
        buttonToggler(false, this.resetButton); // Only while agent in transit.
        buttonToggler(null != getScenarioFile(), this.uploadButton);
        buttonToggler(null != ConsoleData.getScenarioName(), this.executeButton);
        buttonToggler(null != ConsoleData.getSelectedExecution(), this.chartsButton, this.pdfButton);

        // For each agent, disable the buttons that its state does not support.
        for (ConsoleAgent agent : ConsoleData.getSelectedAgents()) {
            // System.err.println("agent: " + agent);

            if (agent.isTransitting()) {
                // System.err.println("transitting: reset on, actions off");
                buttonToggler(true, this.resetButton);
                buttonToggler(false, this.uploadButton, this.deployButton, this.executeButton, this.chartsButton,
                        this.pdfButton);
            }

            else if (agent.getState() == null)
                // System.err.println("no state: actions off");
                buttonToggler(false, this.uploadButton, this.deployButton, this.executeButton, this.chartsButton,
                        this.pdfButton);
            else
                switch (agent.getState()) {
                    case RESET:
                        // System.err
                        // .println("reset state; actions off except upload: "
                        // + this.uploadButton.isEnabled());
                        buttonToggler(false, this.deployButton, this.executeButton, this.chartsButton, this.pdfButton);
                    break;

                    case UPLOAD:
                        // System.err
                        // .println(
                        // "uploaded state; actions off except upload, deploy: "
                        // + this.uploadButton.isEnabled()
                        // + ", "
                        // + this.deployButton.isEnabled());
                        buttonToggler(false, this.executeButton, this.chartsButton, this.pdfButton);
                    break;

                    case DEPLOY:
                        // System.err
                        // .println(
                        // "deployed state; actions off except upload, deploy, execute: "
                        // + this.uploadButton.isEnabled()
                        // + ", "
                        // + this.deployButton.isEnabled()
                        // + ", "
                        // + this.executeButton.isEnabled());
                        buttonToggler(false, this.chartsButton, this.pdfButton);
                    break;

                    case EXECUTE:
                    case CHART:
                        // System.err.println("charted state; do nothing");
                    break;
                }

            highlight(this.uploadButton, AgentState.UPLOAD.equals(agent.getTransit()));
            highlight(this.deployButton, AgentState.DEPLOY.equals(agent.getTransit()));
            highlight(this.executeButton, AgentState.EXECUTE.equals(agent.getTransit()));
            highlight(this.chartsButton, AgentState.CHART.equals(agent.getTransit()));
        }
    }

    private void buttonToggler(boolean enable, JButton... buttons) {

        for (JButton button : buttons)
            button.setEnabled(enable);
    }

    private void highlight(JButton button, boolean highlightOn) {

        int style = highlightOn? Font.ITALIC | Font.BOLD: 0;
        button.setFont(button.getFont().deriveFont(style));
    }

    /**
     * Parse the picked or typed file out of the scenario file field.
     * 
     * @return <code>null</code> if the file is non-existing, unreadable, or not a file.
     */
    private File getScenarioFile() {

        File scenarioFile = null;
        if (null != this.scenarioField.getText()) {
            scenarioFile = new File(this.scenarioField.getText());
            if (scenarioFile.isFile() && scenarioFile.canRead())
                return scenarioFile;
        }

        return null;
    }
}
