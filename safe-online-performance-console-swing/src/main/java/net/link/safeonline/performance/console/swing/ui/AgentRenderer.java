/**
 * 
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.link.safeonline.performance.console.swing.data.Agent;

/**
 * This class is used for generating a {@link Component} that acts as a
 * representational stamp for each agent in the list. It retrieves the agent's
 * status and uses this to generate a contextual representation of the agent
 * that visualises its current status to the user.<br>
 * 
 * @author mbillemo
 */
public class AgentRenderer extends DefaultListCellRenderer {

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

				Throwable err = agent.getError();
				do {
					error = String.format(": %s: %s", err.getClass().getName(),
							err.getMessage());
					err = err.getCause();
				} while (err != null);
			}

			if (agent.getAction() != null)
				switch (agent.getAction()) {
				case DEPLOY:
				case UPLOAD:
					color = "blue";
					break;
				case EXECUTE:
					color = "gray";
					break;
				default:
				}

			setText(String
					.format(
							"<html><ul><li style='color: %s; font-family: monospace'>%s%s</li></ul></html>",
							color, agent, error));
		}

		return this;
	}
}
