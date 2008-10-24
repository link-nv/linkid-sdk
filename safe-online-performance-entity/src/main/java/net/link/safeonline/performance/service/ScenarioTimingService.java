/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service;

import java.util.LinkedList;

import javax.ejb.Local;

import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;


/**
 * <h2>{@link ScenarioTimingService}<br>
 * <sub>Service bean for {@link ScenarioTimingEntity}.</sub></h2>
 * 
 * <p>
 * Retrieve timing data for executions.
 * </p>
 * 
 * <p>
 * <i>Mar 3, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface ScenarioTimingService {

    public static final String BINDING = "SafeOnline/ScenarioTimingService";


    public LinkedList<ScenarioTimingEntity> getExecutionTimings(ExecutionEntity execution, int dataPoints);
}
