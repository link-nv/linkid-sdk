/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Frame;
import java.awt.GridLayout;
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
import javax.swing.WindowConstants;

import net.link.safeonline.performance.console.swing.data.Agent;

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
		this.frame.addWindowListener(this);
		this.frame.setContentPane(this.agents);
		this.frame.pack();
		this.frame.setState(Frame.MAXIMIZED_BOTH);
		this.frame.setVisible(true);
	}

	private void addTab(Address agent, List<byte[]> chartList) {

		for (int tab = 0; tab < this.agents.getTabCount(); ++tab)
			if (this.agents.getTitleAt(tab).equals(agent.toString()))
				return;

		JPanel agentCharts = new JPanel(new GridLayout(0, 1, 0, 20));
		agentCharts.setBorder(Borders.DLU4_BORDER);
		for (byte[] chart : chartList)
			agentCharts.add(new JLabel(new ImageIcon(chart)));

		this.agents.addTab(agent.toString(), new JScrollPane(agentCharts));
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void windowClosed(WindowEvent e) {

		this.frame = null;
		instance = null;
	}

	/**
	 * TODO: Describe method.
	 */
	public static void display(Collection<Agent> agents) {

		if (instance == null)
			instance = new Charts();

		for (Agent agent : agents)
			instance.addTab(agent.getAddress(), agent.getCharts());
	}
}
