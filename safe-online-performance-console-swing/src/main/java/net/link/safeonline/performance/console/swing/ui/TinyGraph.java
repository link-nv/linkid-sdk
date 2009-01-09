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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

    private static final long       serialVersionUID = 1L;

    private int                     values;
    private SortedMap<Long, Double> queue;
    private SortedSet<Long>         times;


    public TinyGraph(int values) {

        this.values = values;
        queue = new TreeMap<Long, Double>();
        times = new TreeSet<Long>();

        setBorder(BorderFactory.createLineBorder(Color.gray));
        setBackground(Color.white);
        setForeground(Color.red);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        if (isOpaque() && getBackground() != null) {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (times.isEmpty())
            return;

        Long startTime = times.first();
        Long endTime = times.last();

        Double minValue = Double.MAX_VALUE;
        Double maxValue = 0d;
        for (Double value : queue.values()) {
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
        }

        if (startTime.equals(endTime) || minValue.equals(maxValue))
            return;

        double timeToPx = getWidth() / (double) (endTime - startTime);
        double valueToPx = getHeight() / (maxValue - minValue);

        Long lastTime = startTime;
        Double lastValue = queue.get(lastTime);
        for (Long time : times) {
            Double value = queue.get(time);
            int currX = (int) ((time - startTime) * timeToPx);
            int lastX = (int) ((lastTime - startTime) * timeToPx);
            int currY = (int) ((value - minValue) * valueToPx);
            int lastY = (int) ((lastValue - minValue) * valueToPx);

            lastTime = time;
            lastValue = value;

            g2.setPaint(new GradientPaint(0, 0, Color.decode("#FFF5F5"), 0, getHeight(), Color.decode("#F5FFF5")));
            g2.fillPolygon(new int[] { lastX, lastX, currX, currX }, new int[] { getHeight(), getHeight() - lastY, getHeight() - currY,
                    getHeight() }, 4);

            g2.setPaint(getForeground());
            g2.drawLine(lastX, getHeight() - lastY, currX, getHeight() - currY);
        }
    }

    /**
     * Update this graph with a new value.
     */
    public void update(Double value) {

        if (value == null)
            return;

        long time = System.currentTimeMillis();
        queue.put(time, value);
        times.add(time);

        /* Clean up overflow. */
        while (queue.size() > values) {
            Long purgeTime = times.first();

            queue.remove(purgeTime);
            times.remove(purgeTime);
        }

        repaint();
    }

    /**
     * Remove all graph values.
     */
    public void reset() {

        queue.clear();
        times.clear();
    }
}
