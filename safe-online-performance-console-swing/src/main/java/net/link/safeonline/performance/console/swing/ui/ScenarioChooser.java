package net.link.safeonline.performance.console.swing.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;

import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.data.Agent.State;
import net.link.safeonline.performance.console.swing.model.ScenarioDeployerThread;
import net.link.safeonline.performance.console.swing.model.ScenarioExecutorThread;
import net.link.safeonline.performance.console.swing.model.ScenarioUploaderThread;

import org.jgroups.Address;

/**
 * This class keeps and listens to the UI components that upload, deploy and
 * execute scenarios on agents.
 * 
 * @author mbillemo
 */
public class ScenarioChooser extends JPanel implements ActionListener,
		CaretListener {

	private static final long serialVersionUID = 1L;

	private ConsoleData consoleData;
	private JButton browseButton;
	private JButton resetButton;
	private JButton uploadButton;
	private JButton deployButton;
	private JButton executeButton;
	private JButton chartsButton;

	protected JPanel sideButton;
	protected JPanel actionButton;
	protected JTextField scenarioField;

	public ScenarioChooser(ConsoleData consoleData) {

		this.consoleData = consoleData;

		this.scenarioField = new JTextField(
				"/Users/mbillemo/Documents/design/safe-online/safe-online-performance-scenario-deploy/target/safe-online-performance-scenario-deploy-1.0-SNAPSHOT.ear");
		this.scenarioField.addCaretListener(this);
		this.browseButton = new JButton("Browse ...");
		this.browseButton.addActionListener(this);
		this.uploadButton = new JButton("Upload this Scenario");
		this.uploadButton.addActionListener(this);
		this.resetButton = new JButton("Reset");
		this.resetButton.addActionListener(this);
		this.deployButton = new JButton("Deploy this Scenario");
		this.deployButton.addActionListener(this);
		this.executeButton = new JButton("Execute this Scenario");
		this.executeButton.addActionListener(this);
		this.chartsButton = new JButton("View Charts on this Scenario");
		this.chartsButton.addActionListener(this);

		this.sideButton = new JPanel(new BorderLayout());
		this.actionButton = new JPanel(new GridLayout(1, 0, 10, 5));
		this.actionButton.add(this.uploadButton);
		this.actionButton.add(this.deployButton);
		this.actionButton.add(this.executeButton);
		this.actionButton.add(this.chartsButton);

		this.uploadButton.setEnabled(null != getScenarioFile());
		enableButtonsFor(State.RESET);
	}

	/**
	 * @{inheritDoc}
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

		else if (this.resetButton.equals(e.getSource()))
			for (Agent agent : this.consoleData.getAgents().values())
				agent.reset();

		else if (this.uploadButton.equals(e.getSource()))
			new ScenarioUploaderThread(getSelectedAgents(), this,
					getScenarioFile()).run();

		else if (this.deployButton.equals(e.getSource()))
			new ScenarioDeployerThread(getSelectedAgents(), this).run();

		else if (this.executeButton.equals(e.getSource()))
			new ScenarioExecutorThread(getSelectedAgents(), this,
					this.consoleData).run();

		else if (this.chartsButton.equals(e.getSource()))
			Charts.display(getSelectedAgents().values());
	}

	private Map<Address, Agent> getSelectedAgents() {

		Map<Address, Agent> selectedAgents = new HashMap<Address, Agent>();
		for (Map.Entry<Address, Agent> agentEntry : this.consoleData
				.getAgents().entrySet())
			if (agentEntry.getValue().isSelected())
				selectedAgents.put(agentEntry.getKey(), agentEntry.getValue());

		return selectedAgents;
	}

	/**
	 * @{inheritDoc}
	 */
	public void caretUpdate(CaretEvent e) {

		if (e.getSource().equals(this.scenarioField))
			for (Agent agent : this.consoleData.getAgents().values())
				agent.fireAgentStatus();
	}

	/**
	 * Disable buttons while working.
	 */
	public void disableButtons() {

		this.resetButton.setEnabled(false);
		this.uploadButton.setEnabled(false);
		this.deployButton.setEnabled(false);
		this.executeButton.setEnabled(false);
		this.chartsButton.setEnabled(false);
	}

	/**
	 * Enable the right buttons. Disable the ones that shouldn't be touched.
	 */
	public void enableButtonsFor(State currentState) {

		this.sideButton.removeAll();

		switch (currentState) {
		case RESET:
			this.sideButton.add(this.browseButton);
			this.resetButton.setEnabled(false);
			this.uploadButton.setEnabled(null != getScenarioFile());
			this.deployButton.setEnabled(false);
			this.executeButton.setEnabled(false);
			this.chartsButton.setEnabled(false);
			break;

		case UPLOAD:
			this.sideButton.add(this.resetButton);
			this.resetButton.setEnabled(true);
			this.uploadButton.setEnabled(null != getScenarioFile());
			this.deployButton.setEnabled(true);
			this.executeButton.setEnabled(false);
			this.chartsButton.setEnabled(false);
			break;

		case DEPLOY:
			this.sideButton.add(this.resetButton);
			this.resetButton.setEnabled(true);
			this.uploadButton.setEnabled(null != getScenarioFile());
			this.deployButton.setEnabled(false);
			this.executeButton.setEnabled(true);
			this.chartsButton.setEnabled(false);
			break;

		case EXECUTE:
			this.sideButton.add(this.resetButton);
			this.resetButton.setEnabled(true);
			this.uploadButton.setEnabled(null != getScenarioFile());
			this.deployButton.setEnabled(false);
			this.executeButton.setEnabled(true);
			this.chartsButton.setEnabled(true);
			break;
		}

		this.sideButton.validate();
		this.sideButton.repaint();
	}

	/**
	 * Parse the picked or typed file out of the scenario file field.
	 * 
	 * @return <code>null</code> if the file is non-existing, unreadable, or
	 *         not a file.
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
