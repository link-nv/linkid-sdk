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
package net.link.safeonline.performance.scenario.charts;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;


/**
 * <h2>{@link AbstractChart}<br>
 * <sub>The basis of chart generators.</sub></h2>
 * 
 * <p>
 * This class implements several helper methods that will be very convenient in generating and rendering charts.<br>
 * <br>
 * You must override and return <code>true</code> on at least one of the following methods:
 * <ul>
 * <li>{@link #isDataProcessed()}</li>
 * <li>{@link #isErrorProcessed()}</li>
 * <li>{@link #isTimingProcessed()}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <i>Feb 22, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public abstract class AbstractChart implements Chart {

    final Log                                    LOG             = LogFactory.getLog(getClass());

    private static ImageEncoder                  encoder         = ImageEncoderFactory.newInstance("png", 0.9f, true);

    protected String                             title;
    private boolean                              linked;
    private List<AbstractChart>                  links;

    private XYPlot                               plot;
    private static Map<Collection<Chart>, Range> sharedRangesMap = new HashMap<Collection<Chart>, Range>();


    public AbstractChart(String title) {

        this.title = title;
        this.linked = false;
        this.links = new ArrayList<AbstractChart>();
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle() {

        return this.title;
    }

    /**
     * {@inheritDoc}
     * 
     * Post-processing here calculates ranges for shared axes.<br>
     * We also cache the plot for use in {@link #render(int)}.
     */
    public void postProcess() {

        this.plot = getPlot();
        if (this.plot == null)
            return;
        if (this.plot.getDomainAxis() == null) {
            this.LOG.warn("Plot for " + getClass().getName() + " has no domain axis!");
            return;
        }

        for (Map.Entry<Collection<Chart>, Range> entry : sharedRangesMap.entrySet()) {
            Collection<Chart> charts = entry.getKey();
            Range range = entry.getValue();

            if (charts.contains(this)) {
                Range plotRange = this.plot.getDomainAxis().getRange();
                sharedRangesMap.put(charts, Range.combine(range, plotRange));

                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte[][] render(int dataPoints) {

        // Don't render when linked or when no plot was prepared.
        if (this.linked || this.plot == null)
            return null;

        // Apply the shared range if this chart is in the shared ranges map.
        for (Map.Entry<Collection<Chart>, Range> entry : sharedRangesMap.entrySet()) {
            Collection<Chart> charts = entry.getKey();
            Range range = entry.getValue();

            if (charts.contains(this)) {
                this.plot.getDomainAxis().setRange(range);

                break;
            }
        }

        // Shove all linked charts in one plot.
        if (!this.links.isEmpty()) {
            XYPlot basePlot = this.plot;
            CombinedDomainXYPlot combinedPlot;

            this.plot = combinedPlot = new CombinedDomainXYPlot(basePlot.getDomainAxis());

            combinedPlot.add(basePlot);

            for (AbstractChart link : this.links) {
                XYPlot linkedPlot = link.getPlot();
                if (linkedPlot != null) {
                    combinedPlot.add(linkedPlot);
                }
            }
        }

        // Not linked, add average markers.
        else {
            XYDataset set = this.plot.getDataset();
            if (set != null) {
                for (int i = 0; i < set.getSeriesCount(); ++i) {
                    double sum = 0;
                    for (int j = 0; j < set.getItemCount(i); ++j) {
                        sum += set.getYValue(i, j);
                    }

                    ValueMarker marker = new ValueMarker(sum / set.getItemCount(i));
                    marker.setLabel("Average " + i + "                ");
                    marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                    this.plot.addRangeMarker(marker);
                }
            }
        }

        JFreeChart chart = new JFreeChart(this.plot);
        return new byte[][] { getImage(chart, dataPoints) };
    }

    /**
     * Implement this method to generate the plot that depicts the chart your module generates.
     */
    protected abstract XYPlot getPlot();

    /**
     * {@inheritDoc}
     */
    public void processData(ProfileDataEntity data) {

    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataProcessed() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void processError(DriverExceptionEntity error) {

    }

    /**
     * {@inheritDoc}
     */
    public boolean isErrorProcessed() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void processTiming(ScenarioTimingEntity data) {

    }

    /**
     * {@inheritDoc}
     */
    public boolean isTimingProcessed() {

        return false;
    }

    /**
     * Link this plot with the given other plots.<br>
     * <br>
     * The other plots will all use the domain axis of this plot and will not generate a chart of their own. A single chart will be
     * generated for all plots linked to this one.<br>
     * <br>
     * <b>NOTE: Do not chain link plots, links do not work recursively. All plots beyond the first level of the chain will no longer be
     * visible in the charts.</b>
     */
    public void linkWith(AbstractChart... charts) {

        for (AbstractChart chart : charts) {
            chart.isLinked();
            this.links.add(chart);
        }
    }

    private void isLinked() {

        this.linked = true;
    }

    protected Long getMeasurement(Set<MeasurementEntity> measurements, String type) throws NoSuchElementException {

        for (MeasurementEntity e : measurements)
            if (type.equals(e.getMeasurement()))
                return e.getDuration();

        this.LOG.debug("for: " + measurements);
        throw new NoSuchElementException("Element " + type + " could not be found.");
    }

    protected boolean isEmpty(Map<String, ? extends Series> data) {

        for (Series series : data.values())
            if (!series.isEmpty())
                return false;

        return true;
    }

    protected byte[] getImage(JFreeChart chart, int width) {

        return getImage(chart, width, width);
    }

    protected byte[] getImage(JFreeChart chart, int width, int height) {

        try {
            chart.setBackgroundPaint(Color.white);
            return encoder.encode(chart.createBufferedImage(width, height));
        } catch (IOException e) {
            return null;
        }
    }

    // FIXME
    <S> S getService(Class<S> service, String binding) {

        try {
            InitialContext initialContext = new InitialContext();
            return service.cast(initialContext.lookup(binding));
        } catch (NoInitialContextException e) {
            try {
                return service.cast(Class.forName(service.getName().replaceFirst("\\.([^\\.]*)$", ".bean.$1Bean")).newInstance());
            } catch (InstantiationException ee) {
                this.LOG.error("Couldn't create service " + service + " at " + binding, ee);
                throw new RuntimeException(ee);
            } catch (IllegalAccessException ee) {
                this.LOG.error("Couldn't access service " + service + " at " + binding, ee);
                throw new RuntimeException(ee);
            } catch (ClassNotFoundException ee) {
                this.LOG.error("Couldn't find service " + service.getName().replaceFirst("\\.([^\\.]*)$", ".bean.$1Bean") + " at "
                        + binding, ee);
                throw new RuntimeException(ee);
            }
        } catch (NamingException e) {
            this.LOG.error("Couldn't find service " + service + " at " + binding, e);
            throw new RuntimeException(e);
        }
    }

    public static void sharedTimeAxis(Collection<Chart> charts) {

        sharedRangesMap.put(charts, null);
    }
}
