/**
 * 
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.link.safeonline.performance.console.swing.data.ConsoleData;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * A Swing UI that provides interaction with and visual reporting of the status
 * of agents in the profiling group.<br>
 * <br>
 * This class takes care of the initialisation of the frame and its lay-out.
 * 
 * @author mbillemo
 */
public class SwingConsole {

	private ConsoleData consoleData;

	public SwingConsole() {

		this.consoleData = new ConsoleData();

		initLnF();
		buildUI();
	}

	private void initLnF() {

		try {
			System.setProperty("swing.aatext", "true");
			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
	}

	private void buildUI() {

		// Content pane.
		JPanel pane = new JPanel(new BorderLayout(5, 10));
		pane.setBorder(Borders.DLU7_BORDER);

		// Frame.
		JFrame frame = new JFrame("SafeOnline Performance Testing Console");
		frame.setPreferredSize(new Dimension(800, 500));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(pane);

		// Objects for each paragraph.
		// They will handle their components' events.
		ScenarioChooser scenarioChooser = new ScenarioChooser(this.consoleData);
		AgentsList agentsList = new AgentsList(this.consoleData,
				scenarioChooser);
		OlasPrefs olasPrefs = new OlasPrefs(this.consoleData);

		// JGoodies Forms layout definition.
		FormLayout layout = new FormLayout("p, 5dlu, 0:g, 5dlu, p, 5dlu, p",
				"p, 5dlu, t:p:g, 10dlu, p, 5dlu, p, 10dlu, p, 5dlu, p, 5dlu, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, pane);

		builder.appendSeparator("Members of group "
				+ this.consoleData.getAgentDiscoverer().getGroupName());
		builder.nextRow();

		builder.append(agentsList, 7);
		builder.nextRow();

		builder.appendSeparator("OLAS Location");
		builder.nextRow();

		builder.append("Hostname:", olasPrefs.hostname);
		builder.append("Port:", olasPrefs.port);
		builder.nextRow();

		builder.appendSeparator("Scenario Deployer");
		builder.nextRow();

		builder.append("EAR Package:", scenarioChooser.scenarioField);
		builder.append(scenarioChooser.sideButton, 3);
		builder.nextRow();
		builder.append(scenarioChooser.actionButton, 7);

		// Visualise the lot.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {

		new SwingConsole();
	}
}
