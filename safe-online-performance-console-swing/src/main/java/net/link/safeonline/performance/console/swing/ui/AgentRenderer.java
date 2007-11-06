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
 * <br>
 * For the benefit of code simplicity this object assumes it will only be set as
 * listener for one {@link JList}. Please do not break this contract or not all
 * lists will be notified when agent status changes.
 * 
 * @author mbillemo
 */
public class AgentRenderer extends DefaultListCellRenderer implements
		AgentStatusListener {

	private static final long serialVersionUID = 1L;

	private JList agentsList;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		this.agentsList = list;
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		if (value instanceof Agent) {
			Agent agent = (Agent) value;
			agent.setAgentStatusListener(this);

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
			if (agent.isDeploying() || agent.isUploading())
				color = "blue";
			if (agent.isExecuting())
				color = "gray";

			setText(String
					.format(
							"<html><ul><li style='color: %s; font-family: monospace'>%s%s</li></ul></html>",
							color, agent, error));
		}

		return this;
	}

	/**
	 * @{inheritDoc}
	 */
	public void statusChanged(Agent agent) {

		this.agentsList.repaint();
	}
}
