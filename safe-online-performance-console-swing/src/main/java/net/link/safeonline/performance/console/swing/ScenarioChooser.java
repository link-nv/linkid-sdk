package net.link.safeonline.performance.console.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author mbillemo
 * 
 */
public class ScenarioChooser extends JPanel implements ActionListener {

	private static Log scLOG = LogFactory.getLog(ScenarioChooser.class);

	private static final long serialVersionUID = 1L;

	private JTextField scenarioField;
	private JButton browseButton;
	private JButton resetButton;
	private JButton uploadButton;
	private JButton deployButton;
	private AgentsList agents;

	private JPanel sideButton;

	private JPanel actionButton;

	public ScenarioChooser(AgentsList agents) {

		this.agents = agents;

		this.scenarioField = new JTextField(
				"/Users/mbillemo/Documents/design/safe-online/safe-online-deploy/target/SafeOnline.ear");
		this.scenarioField.addActionListener(this);
		this.browseButton = new JButton("Browse ...");
		this.browseButton.addActionListener(this);
		this.uploadButton = new JButton("Upload This Scenario");
		this.uploadButton.addActionListener(this);
		this.resetButton = new JButton("Reset");
		this.resetButton.addActionListener(this);
		this.deployButton = new JButton("Deploy This Scenario");
		this.deployButton.addActionListener(this);

		this.sideButton = new JPanel(new BorderLayout());
		this.actionButton = new JPanel(new BorderLayout());

		FormLayout layout = new FormLayout("p, 5dlu, p:g, 5dlu, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);

		builder.append("EAR Package:", this.scenarioField);
		builder.append(this.sideButton);
		builder.append(this.actionButton, 5);

		setReadyForDeployment(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {

		if (this.browseButton.equals(e.getSource())) {

			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {

					return f.getName().endsWith(".ear");
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
			new ScenarioUploaderThread(this.agents, this, getScenarioFile())
					.start();

		else if (this.resetButton.equals(e.getSource()))
			setReadyForDeployment(false);

		else if (this.deployButton.equals(e.getSource()))
			new ScenarioDeployerThread(this.agents, this).start();

		// Enable upload button if scenario file is valid.
		this.uploadButton.setEnabled(null != getScenarioFile());
	}

	/**
	 * Show the right buttons.
	 */
	protected void setReadyForDeployment(boolean readyForDeploy) {

		scLOG.debug("ready for deployment: " + readyForDeploy);

		this.sideButton.removeAll();
		this.actionButton.removeAll();

		if (!readyForDeploy) {
			this.sideButton.add(this.browseButton);
			this.actionButton.add(this.uploadButton);
		}

		else {
			this.sideButton.add(this.resetButton);
			this.actionButton.add(this.deployButton);
		}

		validate();
		repaint();
	}

	/**
	 * Disable buttons that shouldn't be used.
	 */
	protected void setButtonsEnabled(boolean buttonsEnabled) {

		this.uploadButton.setEnabled(buttonsEnabled);
		this.deployButton.setEnabled(buttonsEnabled);
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
