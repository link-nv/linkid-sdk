/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
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

	public SwingConsole() {

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

		// Objects for each paragraph.
		// They will handle their components' events.
		ScenarioChooser scenarioChooser = new ScenarioChooser();
		ExecutionInfo executionInfo = new ExecutionInfo();
		AgentsList agentsList = new AgentsList();
		OlasPrefs olasPrefs = new OlasPrefs();

		// Make these listen to agent selection events.
		ConsoleData.addAgentSelectionListener(executionInfo);
		ConsoleData.addAgentSelectionListener(scenarioChooser);
		ConsoleData.addExecutionSelectionListener(scenarioChooser);

		// JGoodies Forms layout definition.
		FormLayout layout = new FormLayout(
				"p, 5dlu, 0:g, 5dlu, p",
				"p, 5dlu, f:0dlu:g, 10dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 10dlu, p, 5dlu, p");
		layout.setColumnGroups(new int[][] { { 1, 5 } });
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Group "
				+ ConsoleData.getAgentDiscoverer().getGroupName());
		builder.nextRow();

		JScrollPane executionInfoPane = new JScrollPane(executionInfo);
		executionInfoPane.getViewport().setBackground(
				executionInfo.getBackground());
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(agentsList), executionInfoPane);
		builder.append(split, 5);
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

		builder.append("Duration:");
		builder.append("EAR Package:");
		builder.nextColumn();
		builder.nextRow();

		builder.append(olasPrefs.duration);
		builder.append(scenarioChooser.scenarioField);
		builder.append(scenarioChooser.browseButton);
		builder.nextRow();

		builder.appendSeparator("Actions");
		builder.nextRow();

		builder.append(scenarioChooser.actionButtons, 5);

		// Frame.
		final JFrame frame = new JFrame(
				"SafeOnline Performance Testing Console");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(builder.getPanel());

		// Visualise the lot.
		frame.pack();
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// Center the divider.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				if (frame.isVisible())
					split.setDividerLocation(0.5);
				else
					SwingUtilities.invokeLater(this);
			}
		});
	}

	public static void main(String[] args) {

		new SwingConsole();
	}
}
