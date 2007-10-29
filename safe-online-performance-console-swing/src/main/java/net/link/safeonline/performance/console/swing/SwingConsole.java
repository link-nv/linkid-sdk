/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.link.safeonline.performance.console.jgroups.AgentDiscoverer;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * A Swing UI that provides interaction with and visual reporting of the status
 * of agents in the profiling group.
 * 
 * @author mbillemo
 * 
 */
public class SwingConsole {

	private AgentDiscoverer agentDiscoverer;

	public SwingConsole() {

		this.agentDiscoverer = new AgentDiscoverer();

		initLnF();
		buildUI();
	}

	private void initLnF() {

		try {
			System.setProperty("swing.aatext", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
	}

	private void buildUI() {

		JFrame frame = new JFrame();
		JPanel pane = new JPanel(new BorderLayout(5, 10));
		pane.setBorder(Borders.DLU7_BORDER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(pane);

		AgentsList agents = new AgentsList(this.agentDiscoverer);
		ScenarioChooser chooser = new ScenarioChooser(agents);

		FormLayout layout = new FormLayout("p:g",
				"p, 5dlu, t:p:g, 5dlu, p, 5dlu, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, pane);
		builder.appendSeparator("Members of group "
				+ this.agentDiscoverer.getGroupName());

		builder.nextRow();
		builder.append(agents);

		builder.nextRow();
		builder.appendSeparator("Scenario Deployer");

		builder.nextRow();
		builder.append(chooser);

		frame.setVisible(true);
	}

	public static void main(String[] args) {

		new SwingConsole();
	}
}
