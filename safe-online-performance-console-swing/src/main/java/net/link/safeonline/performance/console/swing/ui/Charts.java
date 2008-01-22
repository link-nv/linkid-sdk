/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.WindowConstants;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;

import org.jgroups.Address;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.Options;

/**
 * @author mbillemo
 *
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

		String tabTitle = String.format("%s (%d workers)", agent.toString(),
				scenarioExecution.getWorkers());
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

	public static void display(Collection<ConsoleAgent> agents) {

		if (instance == null)
			instance = new Charts();

		for (ConsoleAgent agent : agents)
			instance.addTab(agent.getAddress(), agent.getStats());

		instance.show();
	}

	private void show() {

		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	private static class AgentCharts extends JPanel implements Scrollable {

		private static final long serialVersionUID = 1L;
		private static final int INCREMENT = 50;

		public AgentCharts(ScenarioExecution scenarioExecution) {

			FormLayout layout = new FormLayout("p");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
			builder.setDefaultDialogBorder();

			builder.appendTitle(String.format("%s: %d workers x %d agents",
					scenarioExecution.getHostname(), scenarioExecution
							.getWorkers(), scenarioExecution.getAgents()));
			builder.appendTitle(String.format("Duration: %f minutes",
					scenarioExecution.getDuration() / 60000f));
			builder.appendUnrelatedComponentsGapRow();
			builder.nextLine();

			for (Map.Entry<String, byte[][]> charts : scenarioExecution
					.getCharts().entrySet()) {
				builder.appendSeparator(charts.getKey());
				for (byte[] chart : charts.getValue())
					builder.append(new JLabel(new ImageIcon(chart)));
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
