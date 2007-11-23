/**
 * 
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
		initExceptionDialog();
		buildUI();
	}

	private void initLnF() {

		try {
			System.setProperty("swing.aatext", "true");
			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
	}

	private void initExceptionDialog() {

		Thread
				.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

					public void uncaughtException(Thread t, Throwable e) {

						JDialog dialog = new JDialog((Frame) null,
								"Uncaught exception: " + e.getMessage());
						JPanel pane = new JPanel(new BorderLayout());
						pane.setBorder(Borders.DIALOG_BORDER);
						dialog.setContentPane(pane);

						StringWriter writer = new StringWriter();
						e.printStackTrace(new PrintWriter(writer));
						String stackTrace = writer.getBuffer().toString();
						e.printStackTrace();

						pane.add(new JLabel(
								"An uncaught exception was thrown in thread "
										+ t.toString() + ":"),
								BorderLayout.NORTH);
						pane.add(new JTextArea(stackTrace));

						dialog.pack();
						dialog.setLocationRelativeTo(null);
						dialog.setVisible(true);
					}
				});
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
		FormLayout layout = new FormLayout("p, 5dlu, 0:g, 5dlu, p",
				"p, 5dlu, t:p:g, 10dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 10dlu, p, 5dlu, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, pane);

		builder.appendSeparator("Members of group "
				+ this.consoleData.getAgentDiscoverer().getGroupName());
		builder.nextRow();

		builder.append(agentsList, 5);
		builder.nextRow();

		builder.appendSeparator("Preferences");
		builder.nextRow();

		builder.append("Workers:");
		builder.append("Hostname:");
		builder.append("Port:");
		builder.nextRow();
		builder.append(olasPrefs.workers);
		builder.append(olasPrefs.hostname);
		builder.append(olasPrefs.port);
		builder.nextRow();

		builder.append("EAR Package:", scenarioChooser.scenarioField);
		builder.append(scenarioChooser.sideButton);
		builder.nextRow();

		builder.appendSeparator("Actions");
		builder.nextRow();

		builder.append(scenarioChooser.actionButton, 5);

		// Visualise the lot.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {

		new SwingConsole();
	}
}
