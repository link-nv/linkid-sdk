/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.model.AgentSelectionListener;
import net.link.safeonline.performance.console.swing.model.ExecutionSelectionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <h2>{@link AgentPanel}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 2, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class AgentPanel extends JPanel implements MouseListener,
		AgentStatusListener, AgentSelectionListener, ExecutionSelectionListener {

	private static final long serialVersionUID = 1L;

	private ConsoleAgent agent;
	private AgentsList list;

	private boolean selected;

	private JLabel title;
	private JLabel state;
	private JLabel transit;
	private JLabel speed;
	private TinyGraph speedGraph;
	private JProgressBar progress;

	public AgentPanel(AgentsList list, ConsoleAgent agent) {

		this.agent = agent;
		this.list = list;

		buildUi();
		listen(this);

		statusChanged(agent);
		ConsoleData.addAgentStatusListener(this);
		ConsoleData.addAgentSelectionListener(this);
		ConsoleData.addExecutionSelectionListener(this);
	}

	private void listen(Component c) {

		c.addMouseListener(this);

		if (c instanceof Container)
			for (Component cc : ((Container) c).getComponents())
				listen(cc);
	}

	private void buildUi() {

		FormLayout layout = new FormLayout(
				"50dlu, 5dlu, 50dlu, 5dlu, p:g, 5dlu, p", "p, 4dlu, p, 4dlu, p");
		layout.setColumnGroups(new int[][] { { 1, 3 } });
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
		builder.setDefaultDialogBorder();

		builder.append(this.title = new JLabel(), 3);
		this.title.setFont(getFont().deriveFont(18f));
		builder.nextLine(2);

		builder.append(this.state = new JLabel(), this.transit = new JLabel());
		this.state.setFont(getFont().deriveFont(14f));
		this.transit.setFont(getFont().deriveFont(14f));
		builder.nextLine(2);

		builder.append(this.progress = new JProgressBar(0, 1000), 7);

		CellConstraints cc = new CellConstraints();
		builder.add(this.speedGraph = new TinyGraph(100), cc.xywh(5, 1, 1, 3,
				"fill, fill"));
		builder.add(this.speed = new JLabel(), cc.xywh(7, 1, 1, 3,
				"center, fill"));
		this.speed.setFont(getFont().deriveFont(24f));

		setBackground(null);
		this.progress.setOpaque(false);
		this.progress.setBorderPainted(false);
		this.speedGraph.setVisible(false);
	}

	/**
	 * @return The agent of this {@link AgentPanel}.
	 */
	public ConsoleAgent getAgent() {

		return this.agent;
	}

	/**
	 * @return <code>true</code> if the user has this {@link AgentPanel}
	 *         selected.
	 */
	public boolean isSelected() {

		return this.selected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || !(o instanceof AgentPanel))
			return false;

		return this.agent.equals(((AgentPanel) o).agent);
	}

	private void update() {

		/*
		 * Find out which execution is selected and which is this agent's last
		 * started execution.
		 */
		ScenarioExecution selectedExecution = ConsoleData
				.getSelectedExecution();
		ScenarioExecution lastExecution = null;
		if (this.agent.getExecutions() != null
				&& !this.agent.getExecutions().isEmpty())
			lastExecution = new TreeSet<ScenarioExecution>(this.agent
					.getExecutions()).last();

		/* Update all our text values. */
		this.speed.setText("");
		if (this.agent.getExecutions() != null && selectedExecution != null)
			for (ScenarioExecution execution : this.agent.getExecutions())
				if (execution.equalRequest(selectedExecution)) {
					if (execution.getSpeed() != null)
						this.speed.setText(String.format("%.2f/s", execution
								.getSpeed()));

					break;
				}

		if (this.agent.getAddress() != null)
			this.title.setText(this.agent.getAddress().toString());
		else
			this.title.setText("[Unknown]");
		this.title.setForeground(this.state == null ? Color.gray : this.agent
				.isHealthy() ? Color.green.darker() : Color.red);

		if (this.agent.getState() != null) {
			this.state.setText(this.agent.getState().getState());
			this.state.setForeground(this.agent.getState().getColor());
		} else {
			this.state.setText("[Unknown]");
			this.state.setForeground(AgentState.RESET.getColor());
		}

		if (this.agent.isTransitting()) {
			this.transit.setText(this.agent.getTransit().getTransitioning());
			this.transit.setForeground(this.agent.getTransit().getColor());
		} else {
			this.transit.setText(AgentState.RESET.getTransitioning());
			this.transit.setForeground(AgentState.RESET.getColor());
		}

		/* Update the progress bar. */
		if (!this.agent.isTransitting()) {
			this.progress.setIndeterminate(false);
			this.progress.setValue(0);
		} else {

			/* Default to indeterminate. */
			this.progress.setIndeterminate(true);
			this.speedGraph.setVisible(false);

			/* If this agent is executing and there is a last execution.. */
			if (AgentState.EXECUTE.equals(this.agent.getTransit())
					&& lastExecution != null) {

				/* Update speed graph with last execution's speed. */
				this.speedGraph.setVisible(true);
				this.speedGraph.update(lastExecution.getSpeed());

				/* Check how much time is left. */
				long timeLeft = lastExecution.getStartTime().getTime()
						+ lastExecution.getDuration()
						- System.currentTimeMillis();

				/* If there is time left, show a progress bar. */
				if (timeLeft > 0) {
					int completion = (int) (1000 - 1000 * timeLeft
							/ lastExecution.getDuration());

					this.progress.setIndeterminate(false);
					this.progress.setValue(completion);
				}
			} else
				this.speedGraph.reset();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentsSelected(Set<ConsoleAgent> selectedAgents) {

		update();
	}

	/**
	 * {@inheritDoc}
	 */
	public void statusChanged(ConsoleAgent changedAgent) {

		if (this.agent.equals(changedAgent))
			update();
	}

	/**
	 * {@inheritDoc}
	 */
	public void executionSelected(ScenarioExecution execution) {

		update();
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseClicked(MouseEvent e) {

		this.selected = !this.selected;

		if (this.selected)
			setBackground(Color.decode("#EEEEFF"));
		else
			setBackground(null);

		this.list.fireListSelectionChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void mouseReleased(MouseEvent e) {
	}
}
