package net.link.safeonline.performance.console.swing.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;

import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.model.ScenarioDeployerThread;
import net.link.safeonline.performance.console.swing.model.ScenarioExecutorThread;
import net.link.safeonline.performance.console.swing.model.ScenarioUploaderThread;

/**
 * This class keeps and listens to the UI components that upload, deploy and
 * execute scenarios on agents.
 * 
 * @author mbillemo
 */
public class ScenarioChooser extends JPanel implements ActionListener,
		CaretListener {

	private static final long serialVersionUID = 1L;

	private JButton browseButton;
	private JButton resetButton;
	private JButton uploadButton;
	private JButton deployButton;
	private JButton executeButton;

	protected JTextField scenarioField;
	protected JPanel sideButton;
	protected JPanel actionButton;

	private ConsoleData consoleData;

	public ScenarioChooser(ConsoleData consoleData) {

		this.consoleData = consoleData;

		this.scenarioField = new JTextField(
				"/Users/mbillemo/Documents/design/safe-online/safe-online-performance-scenario-deploy/target/safe-online-performance-scenario-deploy-1.0-SNAPSHOT.ear");
		this.scenarioField.addCaretListener(this);
		this.browseButton = new JButton("Browse ...");
		this.browseButton.addActionListener(this);
		this.uploadButton = new JButton("Upload This Scenario");
		this.uploadButton.addActionListener(this);
		this.resetButton = new JButton("Reset");
		this.resetButton.addActionListener(this);
		this.deployButton = new JButton("Deploy This Scenario");
		this.deployButton.addActionListener(this);
		this.executeButton = new JButton("Execute This Scenario");
		this.executeButton.addActionListener(this);

		this.sideButton = new JPanel(new BorderLayout());
		this.actionButton = new JPanel(new BorderLayout());

		this.uploadButton.setEnabled(null != getScenarioFile());
		setDeploymentPhase(DeploymentPhase.UPLOAD);
	}

	/**
	 * @{inheritDoc}
	 */
	public void caretUpdate(CaretEvent e) {

		if (e.getSource().equals(this.scenarioField))
			this.uploadButton.setEnabled(null != getScenarioFile());
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

		else if (this.uploadButton.equals(e.getSource()))
			new ScenarioUploaderThread(this.consoleData.getAgents(), this,
					getScenarioFile()).start();

		else if (this.resetButton.equals(e.getSource()))
			setDeploymentPhase(DeploymentPhase.UPLOAD);

		else if (this.deployButton.equals(e.getSource()))
			new ScenarioDeployerThread(this.consoleData.getAgents(), this)
					.start();

		else if (this.executeButton.equals(e.getSource()))
			new ScenarioExecutorThread(this.consoleData.getAgents(), this,
					this.consoleData.getHostname(), this.consoleData.getPort())
					.start();
	}

	/**
	 * Show the right buttons.
	 */
	public void setDeploymentPhase(DeploymentPhase phase) {

		this.sideButton.removeAll();
		this.actionButton.removeAll();

		switch (phase) {
		case UPLOAD:
			this.sideButton.add(this.browseButton);
			this.actionButton.add(this.uploadButton);
			this.scenarioField.setEnabled(true);
			break;

		case DEPLOY:
			this.sideButton.add(this.resetButton);
			this.actionButton.add(this.deployButton);
			this.scenarioField.setEnabled(false);
			break;

		case EXECUTE:
			this.sideButton.add(this.resetButton);
			this.actionButton.add(this.executeButton);
			this.scenarioField.setEnabled(false);
			break;
		}

		this.sideButton.validate();
		this.sideButton.repaint();
		this.actionButton.validate();
		this.actionButton.repaint();
	}

	/**
	 * Disable buttons that shouldn't be used.
	 */
	public void setButtonsEnabled(boolean buttonsEnabled) {

		this.resetButton.setEnabled(buttonsEnabled);
		this.uploadButton.setEnabled(buttonsEnabled);
		this.deployButton.setEnabled(buttonsEnabled);
		this.executeButton.setEnabled(buttonsEnabled);
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

	public enum DeploymentPhase {
		UPLOAD, DEPLOY, EXECUTE;
	}
}
