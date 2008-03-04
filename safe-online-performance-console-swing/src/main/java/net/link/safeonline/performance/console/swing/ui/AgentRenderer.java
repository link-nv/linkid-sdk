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

			String state = "Unavailable";
			if (agent.getState() == null)
				color = "red";
			else
				state = agent.getState().getState();

			// Trim the bit groups shared by all agents off of the address.
			StringBuffer address = new StringBuffer(agent.getAddress()
					.toString());
			address.delete(address.indexOf(":"), address.length());
			StringBuffer common = new StringBuffer(address);
			for (int i = 0; i < list.getModel().getSize(); ++i) {
				Object current = list.getModel().getElementAt(i);
				if (current == agent || !(current instanceof ConsoleAgent))
					continue;

				String currentAddress = ((ConsoleAgent) current).getAddress()
						.toString();
				for (int j = currentAddress.length() - 1; j > -1; --j)
					if (common.length() > j)
						if (common.charAt(j) != currentAddress.charAt(j))
							common.deleteCharAt(j);
						else
							break;
			}
			if (common.length() > 0 && !common.equals(address)) {
				if (common.indexOf(".") >= 0)
					common.delete(common.lastIndexOf("."), common.length() - 1);
				address.delete(0, common.length() - 1);
				address.insert(0, '~');
			}

			setText(String
					.format(
							"<html><ul><li style='color: %s; font-family:monospace'>%s [%s]%s</li></ul></html>",
							color, address, (action == null ? state : action
									.getTransitioning()), error));
		}

		return this;
	}
}
