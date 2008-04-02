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
import java.awt.Graphics;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * <h2>{@link TinyGraph}<br>
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
public class TinyGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	private int values;
	private SortedMap<Long, Double> queue;
	private SortedSet<Long> times;

	public TinyGraph(int values) {

		this.values = values;
		this.queue = new TreeMap<Long, Double>();
		this.times = new TreeSet<Long>();

		setBorder(BorderFactory.createLineBorder(Color.darkGray));
		setBackground(Color.white);
		setForeground(Color.red);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(Graphics g) {

		if (isOpaque() && getBackground() != null) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		g.setColor(getForeground());

		if (this.times.isEmpty())
			return;

		Long startTime = this.times.first();
		Long endTime = this.times.first();

		Double minValue = Double.MAX_VALUE;
		Double maxValue = 0d;
		for (Double value : this.queue.values()) {
			minValue = Math.min(minValue, value);
			maxValue = Math.max(maxValue, value);
		}

		if (startTime.equals(endTime) || minValue.equals(maxValue))
			return;

		int timeToPx = (int) (getWidth() / (endTime - startTime));
		int valueToPx = (int) (getHeight() * (maxValue - minValue));

		Long lastTime = this.times.first();
		Double lastValue = this.queue.get(lastTime);
		for (Long time : this.times) {
			Double value = this.queue.get(time);
			int currX = (int) ((time - startTime) * timeToPx);
			int lastX = (int) ((lastTime - startTime) * timeToPx);
			int currY = (int) ((value - minValue) * valueToPx);
			int lastY = (int) ((lastValue - minValue) * valueToPx);

			lastTime = time;
			lastValue = value;

			g.drawLine(lastX, lastY, currX, currY);
		}
	}

	/**
	 * Update this graph with a new value.
	 */
	public void update(Double value) {

		long time = System.currentTimeMillis();
		this.queue.put(time, value);
		this.times.add(time);

		/* Clean up overflow. */
		while (this.queue.size() > this.values) {
			Long purgeTime = this.times.first();

			this.queue.remove(purgeTime);
			this.times.remove(purgeTime);
		}
	}
}
