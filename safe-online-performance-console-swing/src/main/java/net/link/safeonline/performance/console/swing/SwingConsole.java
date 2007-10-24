/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.link.safeonline.performance.console.jgroups.AgentDiscoverer;

/**
 * A Swing UI that provides interaction with and visual reporting of the status
 * of agents in the profiling group.
 * 
 * @author mbillemo
 * 
 */
public class SwingConsole {

	private JPanel pane;
	private AgentDiscoverer agentDiscoverer;

	public SwingConsole() {

		this.agentDiscoverer = new AgentDiscoverer();

		buildUI();
	}

	private void buildUI() {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(this.pane = new JPanel(new BorderLayout()));

		this.pane
				.add(new AgentsGrid(this.agentDiscoverer), BorderLayout.CENTER);

		frame.setVisible(true);
	}

	public static void main(String[] args) {

		new SwingConsole();
	}
}
