/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.jfree.chart.JFreeChart;
import org.jgroups.Address;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.looks.Options;

/**
 * @author mbillemo
 * 
 */
public class Charts {

	public Charts(Map<Address, List<JFreeChart>> map) {

		// Tabs.
		JTabbedPane agents = new JTabbedPane();
		agents.setBorder(Borders.DLU4_BORDER);
		agents.putClientProperty(Options.EMBEDDED_TABS_KEY, true);

		// Tab contents.
		for (Address agent : map.keySet()) {

			JPanel agentCharts = new JPanel(new GridLayout(0, 1, 0, 20));
			agentCharts.setBorder(Borders.DLU4_BORDER);
			for (JFreeChart chart : map.get(agent))
				agentCharts.add(new JLabel(new ImageIcon(chart
						.createBufferedImage(700, 500))));

			agents.addTab(agent.toString(), new JScrollPane(agentCharts));
		}

		// Frame.
		JFrame frame = new JFrame("Performance Testing Charts");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800, 600));
		frame.setContentPane(agents);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
