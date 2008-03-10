/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.model.AgentSelectionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <h2>{@link ExecutionInfo}<br>
 * <sub>Display available executions and information about them.</sub></h2>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ExecutionInfo extends JPanel implements ChangeListener,
		AgentSelectionListener, Scrollable, AgentStatusListener {

	private static final long serialVersionUID = 1L;
	static final SimpleDateFormat labelTimeFormat = new SimpleDateFormat(
			"HH:mm");

	private List<ScenarioExecution> executions;
	private JSlider executionSelection;

	private JLabel scenarioName;
	private JLabel startTime;
	private JLabel agents;
	private JLabel workers;
	private JLabel duration;
	private JLabel speed;
	private JLabel hostname;
	private JEditorPane description;
	private JLabel durationLabel;
	private HoverActionAdaptor hoverAdaptor;

	public ExecutionInfo() {

		this.executions = new ArrayList<ScenarioExecution>();

		this.executionSelection = new JSlider(SwingConstants.HORIZONTAL, 0, 0,
				0);

		FormLayout layout = new FormLayout("r:0dlu:g, 10dlu, 0dlu:g");
		layout.setColumnGroups(new int[][] { { 1, 3 } });
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
		builder.setDefaultDialogBorder();
		setBackground(Color.white);

		Font originalDefault = UIManager.getFont("Label.font");
		UIManager.put("Label.font", originalDefault.deriveFont(20f));

		builder.appendSeparator("Completed Executions:");
		builder.append(this.executionSelection, 3);

		builder.appendParagraphGapRow();
		builder.nextLine(2);

		builder.append(this.scenarioName = new JLabel(), 3);

		builder.appendUnrelatedComponentsGapRow();
		builder.nextLine(2);

		builder.append("Initiated:", this.startTime = new JLabel());
		builder.append("Agents Used:", this.agents = new JLabel());
		builder.append("Workers Used:", this.workers = new JLabel());
		builder.append(this.durationLabel = new JLabel(),
				this.duration = new JLabel());
		builder.append("Average Speed:", this.speed = new JLabel());
		builder.append("OLAS Server Hostname:", this.hostname = new JLabel());

		builder.appendSeparator("Description:");
		builder.append(this.description = new JEditorPane("text/html", ""), 3);

		UIManager.put("Label.font", originalDefault);

		this.executionSelection.addChangeListener(this);
		this.executionSelection.setSnapToTicks(true);
		this.executionSelection.setEnabled(false);
		this.executionSelection.setOpaque(false);

		this.scenarioName.setHorizontalAlignment(SwingConstants.CENTER);
		this.scenarioName.setFont(this.scenarioName.getFont().deriveFont(36f));

		this.description.setEditable(false);

		this.hoverAdaptor = new HoverActionAdaptor(this.duration) {
			@Override
			protected void clicked(JComponent component) {

				updateExecutionSelection();
			}
		};

		ConsoleData.addAgentSelectionListener(this);
		ConsoleData.addAgentStatusListener(this);

		setExecution(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stateChanged(ChangeEvent e) {

		if (this.executionSelection.equals(e.getSource()))
			updateExecutionSelection();
	}

	void updateExecutionSelection() {

		synchronized (this.executions) {
			if (this.executionSelection.getValue() < this.executions.size()
					&& this.executionSelection.getValue() > -1)
				setExecution(this.executions.get(this.executionSelection
						.getValue()));
			else
				setExecution(null);
		}
	}

	private void setExecution(ScenarioExecution execution) {

		ConsoleData.setExecution(execution);

		if (execution == null) {
			this.executionSelection.setToolTipText("N/A");
			this.scenarioName.setText("N/A");
			this.description.setText("<pre>N/A</pre>");
			this.startTime.setText("N/A");
			this.agents.setText("N/A");
			this.workers.setText("N/A");
			this.duration.setText("N/A");
			this.speed.setText("N/A");
			this.hostname.setText("N/A");

			this.hoverAdaptor.enable(false, this.duration);
			this.durationLabel.setText("Duration:");
		} else {
			this.executionSelection.setToolTipText(execution.toString());
			this.scenarioName
					.setText(execution.getScenarioName() == null ? "N/A"
							: execution.getScenarioName().replaceFirst(".*\\.",
									""));
			this.description
					.setText(execution.getDuration() == null ? "<pre>N/A</pre>"
							: execution.getScenarioDescription().replaceAll(
									"\n", "<br>"));
			this.startTime.setText(DateFormat.getDateTimeInstance().format(
					execution.getStartTime()));
			this.agents.setText(String.format("%s agent%s", execution
					.getAgents(), execution.getAgents() == 1 ? "" : "s"));
			this.workers.setText(String.format("%s worker%s", execution
					.getWorkers(), execution.getWorkers() == 1 ? "" : "s"));
			this.speed.setText(execution.getSpeed() == null ? "N/A" : String
					.format("%.2f scenario%s/s", execution.getSpeed(),
							execution.getSpeed() == 1 ? "" : "s"));
			this.hostname.setText(execution.getHostname());

			long timeLeft = execution.getStartTime().getTime()
					+ execution.getDuration() - System.currentTimeMillis();
			if (timeLeft > 0) {
				this.durationLabel.setText("Time Remaining:");
				this.duration.setText(formatDuration(timeLeft));
				this.hoverAdaptor.enable(true, this.duration);
			} else {
				this.durationLabel.setText("Duration:");
				this.duration.setText(formatDuration(execution.getDuration()));
				this.hoverAdaptor.enable(false, this.duration);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void statusChanged(ConsoleAgent agent) {

		updateExecutions();
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentsSelected(Set<ConsoleAgent> selectedAgents) {

		updateExecutions();
	}

	private void updateExecutions() {

		Set<ConsoleAgent> selectedAgents = ConsoleData.getSelectedAgents();
		if (selectedAgents == null || selectedAgents.isEmpty()) {
			setExecutions(new HashSet<ScenarioExecution>());
			return;
		}

		// Find the intersection of all executions with the same start time
		// and all scenarios with the same name.
		SortedSet<ScenarioExecution> commonExecutions = new TreeSet<ScenarioExecution>();
		for (ConsoleAgent agent : selectedAgents) {
			Set<ScenarioExecution> agentExecutions = agent.getExecutions();

			if (agentExecutions == null)
				agentExecutions = new HashSet<ScenarioExecution>();

			if (commonExecutions.isEmpty())
				commonExecutions.addAll(agentExecutions);

			else {
				Iterator<ScenarioExecution> it = commonExecutions.iterator();
				while (it.hasNext())
					if (!containsExecution(agentExecutions, it.next()))
						it.remove();
			}
		}

		setExecutions(commonExecutions);
	}

	private void setExecutions(final Set<ScenarioExecution> executions) {

		final JSlider slider = this.executionSelection;
		final List<ScenarioExecution> sliderExecutions = this.executions;

		int maxValue = Math.max(0, executions.size() - 1);
		int selectedExecution = maxValue;
		Date selectedExecutionTime = null;
		if (ConsoleData.getExecution() != null)
			selectedExecutionTime = ConsoleData.getExecution().getStartTime();

		synchronized (sliderExecutions) {
			sliderExecutions.clear();
			sliderExecutions.addAll(executions);

			Dictionary<Integer, JComponent> dictionary = new Hashtable<Integer, JComponent>();
			if (sliderExecutions.isEmpty())
				dictionary.put(0, new JLabel("N/A"));
			else
				for (int i = 0; i < sliderExecutions.size(); ++i) {
					ScenarioExecution execution = sliderExecutions.get(i);
					if (execution.getStartTime().equals(selectedExecutionTime))
						selectedExecution = i;

					dictionary.put(i, new JLabel(labelTimeFormat
							.format(execution.getStartTime())));
				}

			slider.setLabelTable(dictionary);
			slider.setEnabled(!sliderExecutions.isEmpty());
			slider.setPaintLabels(!sliderExecutions.isEmpty());
		}

		slider.getModel().setRangeProperties(selectedExecution, 0, 0, maxValue,
				false);
		updateExecutionSelection();
	}

	private boolean containsExecution(Set<ScenarioExecution> set,
			ScenarioExecution element) {

		for (ScenarioExecution entry : set)
			if (entry.getStartTime() == null && element.getStartTime() == null
					|| entry.getStartTime().equals(element.getStartTime()))
				return true;

		return false;
	}

	/**
	 * Format a time of duration in a human readable manner.
	 *
	 * @param duration
	 *            A duration in ms.
	 */
	public static String formatDuration(long duration) {

		long remainder = duration;

		int weeks = (int) remainder / (7 * 24 * 3600 * 1000);
		remainder %= 7 * 24 * 3600 * 1000;

		int days = (int) remainder / (24 * 3600 * 1000);
		remainder %= 24 * 3600 * 1000;

		int hours = (int) remainder / (3600 * 1000);
		remainder %= 3600 * 1000;

		int minutes = (int) remainder / (60 * 1000);
		remainder %= 60 * 1000;

		int seconds = (int) remainder / 1000;
		remainder %= 1000;

		StringBuffer output = new StringBuffer();
		if (weeks > 0)
			output.append(weeks + " week" + (weeks > 1 ? "s" : "") + ", ");
		if (days > 0)
			output.append(days + " day" + (days > 1 ? "s" : "") + ", ");
		if (hours > 0)
			output.append(hours + " hour" + (hours > 1 ? "s" : "") + ", ");
		if (minutes > 0)
			output
					.append(minutes + " minute" + (minutes > 1 ? "s" : "")
							+ ", ");
		if (seconds > 0)
			output.append(seconds + "s" + ", ");

		if (output.length() < 2)
			return duration + "ms";

		output.delete(output.length() - 2, output.length());
		int lastComma = output.lastIndexOf(",");
		if (lastComma > 0)
			output.replace(lastComma, lastComma + 1, " and");

		return output.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Dimension getPreferredScrollableViewportSize() {

		return getPreferredSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {

		return 100;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getScrollableTracksViewportHeight() {

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getScrollableTracksViewportWidth() {

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {

		return 10;
	}
}
