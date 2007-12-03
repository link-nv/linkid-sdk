/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.WindowConstants;

import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;

import org.jgroups.Address;

import com.jgoodies.forms.factories.Borders;
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
		this.frame.setPreferredSize(new Dimension(640, 480));
		this.frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.frame.setContentPane(this.agents);
		this.frame.addWindowListener(this);
		this.frame.setVisible(true);
	}

	private void addTab(Address agent, List<byte[]> chartList) {

		String tabTitle = String.format("%s (x%d)", agent.toString(),
				ConsoleData.getInstance().getWorkers());
		AgentCharts agentCharts = new AgentCharts(chartList);
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

	public static void display(Collection<Agent> agents) {

		if (instance == null)
			instance = new Charts();

		for (Agent agent : agents)
			instance.addTab(agent.getAddress(), agent.getCharts());
	}

	private static class AgentCharts extends JPanel implements Scrollable {

		private static final long serialVersionUID = 1L;
		private static final int INCREMENT = 50;
		private static GridBagConstraints tabConstraints = new GridBagConstraints(
				0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);

		public AgentCharts(List<byte[]> chartList) {

			super(new GridBagLayout());
			setBorder(Borders.DLU4_BORDER);

			tabConstraints.gridy = 0;
			for (byte[] chart : chartList) {
				add(new JLabel(new ImageIcon(chart)), tabConstraints);

				tabConstraints.gridy++;
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
