/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.awt.Color;

import javax.swing.JToggleButton;

import org.jgroups.Address;

/**
 * @author mbillemo
 * 
 */
public class Agent extends JToggleButton {

	private static final long serialVersionUID = 1L;

	private Address agentAddress;
	private State state;

	/**
	 * Create a new {@link Agent} component based off the agent at the given
	 * {@link Address}.
	 */
	public Agent(Address agentAddress) {

		super(agentAddress.toString(), true);
		setEnabled(true);

		setState(State.READY);
		this.agentAddress = agentAddress;
	}

	/**
	 * Change the state the {@link Agent} is in at the moment.
	 */
	public void setState(State state) {

		this.state = state;
		setBackground(state.getStateColor());
		setForeground(state.getStateColor().brighter());
	}

	/**
	 * Retrieve the state this {@link Agent} is in at the moment.
	 */
	public State getState() {

		return this.state;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		return String.format("[%s] %s", this.state, this.agentAddress);
	}

	/**
	 * The state an agent can be in.
	 * 
	 * @author mbillemo
	 */
	public enum State {

		READY(new Color(0x88BB88)), WORKING(new Color(0x8888BB)), UNRESPONSIVE(
				new Color(0xDDBB88));

		private Color stateColor;

		private State(Color stateColor) {

			this.stateColor = stateColor;
		}

		public Color getStateColor() {
			return this.stateColor;
		}
	}
}
