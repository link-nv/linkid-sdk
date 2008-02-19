/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;

import org.jgroups.Address;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.Options;

/**
 * <h2>{@link Charts}<br>
 * <sub>A window that displays charts.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class Charts extends WindowAdapter {

	private static Charts instance;
	private JTabbedPane agents;
	private JFrame frame;

	private Charts() {

		// Tabs.
		this.agents = new JTabbedPane();
		this.agents.setBorder(Borders.DLU4_BORDER);
		this.agents.putClientProperty(Options.EMBEDDED_TABS_KEY, true);

		// Frame.
		this.frame = new JFrame("Performance Testing Charts");
		this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.frame.setContentPane(this.agents);
		this.frame.addWindowListener(this);
	}

	private void addTab(Address agent, ScenarioExecution scenarioExecution) {

		String tabTitle = String.format("%s (%d worker%s)", agent.toString(),
				scenarioExecution.getWorkers(),
				scenarioExecution.getWorkers() > 1 ? "s" : "");
		AgentCharts agentCharts = new AgentCharts(scenarioExecution);
		this.agents.addTab(tabTitle, new JScrollPane(agentCharts));
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void windowClosed(WindowEvent e) {

		this.frame = null;
		instance = null;
	}

	public static void display() {

		ScenarioExecution execution = ConsoleData.getExecution();
		if (execution == null) {
			JOptionPane.showMessageDialog(null,
					"No (valid) execution selected.",
					"Couldn't find execution.", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (instance == null)
			instance = new Charts();

		for (ConsoleAgent agent : ConsoleData.getSelectedAgents())
			instance.addTab(agent.getAddress(), agent.getStats(execution
					.getId()));

		instance.show();
	}

	private void show() {

		this.frame.setPreferredSize(new Dimension(1024, 768));
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	private static class AgentCharts extends JPanel implements Scrollable {

		private static final long serialVersionUID = 1L;
		private static final int INCREMENT = 50;

		public AgentCharts(ScenarioExecution execution) {

			FormLayout layout = new FormLayout("p");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
			builder.setDefaultDialogBorder();
			setBackground(Color.WHITE);

			Box header = new Box(BoxLayout.PAGE_AXIS);
			builder.append(header);
			JLabel label;

			header.add(label = new JLabel(String.format(
					"[%s] %d worker%s x %d agent%s", execution.getHostname(),
					execution.getWorkers(), execution.getWorkers() > 1 ? "s"
							: "", execution.getAgents(),
					execution.getAgents() > 1 ? "s" : "")));
			label.setFont(label.getFont().deriveFont(30f));

			header.add(label = new JLabel(String.format(
					"Scenario: %s            ", execution.getScenarioName())));
			label.setFont(label.getFont().deriveFont(20f));

			header.add(label = new JLabel(execution.getSpeed() == null ? "N/A"
					: String.format("Average Speed: %.2f scenarios/s",
							execution.getSpeed() * 1000f)));
			label.setFont(label.getFont().deriveFont(20f));

			header.add(label = new JLabel(String.format(
					"Duration: %.2f minutes  ",
					execution.getDuration() / 60000f)));
			label.setFont(label.getFont().deriveFont(20f));

			builder.appendSeparator();
			builder.appendUnrelatedComponentsGapRow();
			builder.nextLine(2);

			for (Map.Entry<String, byte[][]> charts : execution.getCharts()
					.entrySet()) {
				builder.append(label = new JLabel(charts.getKey()));
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setFont(label.getFont().deriveFont(20f));

				for (byte[] chart : charts.getValue())
					builder.append(new JLabel(new ImageIcon(chart)));

				builder.appendSeparator();
			}
		}

		/**
		 * @{inheritDoc}
		 */
		public Dimension getPreferredScrollableViewportSize() {

			return getPreferredSize();
		}

		/**
		 * @{inheritDoc}
		 */
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {

			return INCREMENT * 10;
		}

		/**
		 * @{inheritDoc}
		 */
		public boolean getScrollableTracksViewportHeight() {

			return false;
		}

		/**
		 * @{inheritDoc}
		 */
		public boolean getScrollableTracksViewportWidth() {

			return false;
		}

		/**
		 * @{inheritDoc}
		 */
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {

			return INCREMENT;
		}
	}
}
