/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.util.List;

import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.scenario.charts.Chart;


/**
 * <h2>{@link Scenario}<br>
 * <sub>This interface defines what a {@link Scenario} should implement to be launchable in the agent.</sub></h2>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public interface Scenario extends Runnable {

    /**
     * Prepare execution drivers and load the keys.
     */
    public void prepare(ExecutionEntity execution, ScenarioTimingEntity agentTime);

    /**
     * Implement this method to provide am HTML formatted description string for your scenario: You should explain what
     * it does and how it works.
     */
    public String getDescription();

    /**
     * Specify all charts you'd like to have generated for your scenario.
     */
    public List<? extends Chart> getCharts();
}
