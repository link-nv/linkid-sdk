/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author mbillemo
 * 
 */
public class AgentRenderer extends DefaultListCellRenderer implements
		ListCellRenderer {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		if (value instanceof Agent) {
			Agent agent = (Agent) value;

			String color = "green", error = "";
			if (!agent.isHealthy())
				color = "orange";
			if (null != agent.getError()) {
				color = "red";
				error = ": " + agent.getError().getLocalizedMessage();
			}
			if (agent.isDeploying() || agent.isUploading())
				color = "blue";

			setText(String
					.format(
							"<html><ul><li style='color: %s; font-family: monospace'>%s%s</li></ul></html>",
							color, agent, error));
		}

		return this;
	}
}
