/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;


/**
 * <h2>{@link ScenarioController}<br>
 * <sub>The POJO interface to the {@link ScenarioController}.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface ScenarioController {

    public static final String JNDI_BINDING = "SafeOnline/ScenarioControllerBean";


    /**
     * Execute the scenario.
     */
    public void execute(Date executionId)
            throws Exception;

    /**
     * This method is called before any iterations are executed.<br>
     * <br>
     * These accessors need to be non-<code>null</code>: {@link ExecutionMetadata#getAgents()}, {@link ExecutionMetadata#getWorkers()},
     * {@link ExecutionMetadata#getDuration()}, {@link ExecutionMetadata#getHostname()}.
     */
    public Date prepare(ExecutionMetadata metaData);

    /**
     * Retrieve all scenarios registered for use.
     */
    public Set<String> getScenarios();

    /**
     * Retrieve all available execution IDs (which is the time at which they were initiated).
     */
    public Set<Date> getExecutions();

    /**
     * Retrieve an object that holds all metadata concerning a given execution.
     */
    public ExecutionMetadata getExecutionMetadata(Date executionId);

    /**
     * Retrieve an HTML formatted description string for the given scenario.
     */
    public String getDescription(String scenario);

    /**
     * Retrieve an HTML formatted description string for the given execution (including the scenario and the drivers used by it).
     */
    public String getDescription(Date executionId);

    /**
     * Check what the progress is on the charts generation of the given execution.
     * 
     * @return <code>null</code> when no execution is being charted at the moment. A value ranging between 0 and 1 when charts are being
     *         generated, and 1 when charts are available.
     */
    public Double getProgress(Date executionId);

    /**
     * Create charts on data collected in this scenario.
     */
    public Map<String, byte[][]> createCharts(Date executionId);
}
