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

import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.util.performance.ProfileData;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;


/**
 * <h2>{@link ScenarioQueueChart}<br>
 * <sub>TODO</sub></h2>
 * 
 * <p>
 * </p>
 * 
 * <p>
 * <i>Feb 22, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ScenarioQueueChart extends AbstractChart {

    private TimeSeries auditQueue;


    /**
     * Create a new {@link ScenarioQueueChart} instance.
     */
    public ScenarioQueueChart() {

        super("JMS Queue Size");

        auditQueue = new TimeSeries("Audit", FixedMillisecond.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processData(ProfileDataEntity data) {

        if (data.getMeasurements().isEmpty())
            return;

        FixedMillisecond startTime = new FixedMillisecond(data.getScenarioTiming().getStart());

        Long audit = getMeasurement(data.getMeasurements(), ProfileData.AUDIT_SIZE);

        auditQueue.addOrUpdate(startTime, audit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDataProcessed() {

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected XYPlot getPlot() {

        if (auditQueue.isEmpty())
            return null;

        ValueAxis domainAxis = new DateAxis("Time");

        TimeSeriesCollection auditSet;
        auditSet = new TimeSeriesCollection(auditQueue);

        XYPlot auditPlot = new XYPlot(auditSet, domainAxis, new NumberAxis("Queue Size (messages)"),
                new XYLineAndShapeRenderer(true, false));

        CombinedDomainXYPlot queuePlot = new CombinedDomainXYPlot(domainAxis);
        queuePlot.add(auditPlot);

        return queuePlot;
    }
}
