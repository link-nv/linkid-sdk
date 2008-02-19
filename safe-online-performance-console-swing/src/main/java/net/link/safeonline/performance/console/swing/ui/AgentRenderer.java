/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;

/**
 * <h2>{@link AgentRenderer}<br>
 * <sub>A list renderer for agents.</sub></h2>
 * 
 * <p>
 * This class is used for generating a {@link Component} that acts as a
 * representational stamp for each agent in the list. It retrieves the agent's
 * status and uses this to generate a contextual representation of the agent
 * that visualises its current status to the user.<br>
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
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

		if (value instanceof ConsoleAgent) {
			ConsoleAgent agent = (ConsoleAgent) value;

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

			AgentState action = agent.getTransit();
			if (action != null)
				switch (action) {
				case DEPLOY:
				case UPLOAD:
					color = "blue";
					break;
				case EXECUTE:
					color = "gray";
					break;
				case CHART:
					color = "teal";
					break;
				case RESET:
				}

			AgentState state = agent.getState();
			if (state == null)
				state = AgentState.RESET;

			setText(String
					.format(
							"<html><ul><li style='color: %s; font-family:monospace'>%s [%s]%s</li></ul></html>",
							color, agent.getAddress(), (action == null ? state
									.getState() : action.getTransitioning()),
							error));
		}

		return this;
	}
}
