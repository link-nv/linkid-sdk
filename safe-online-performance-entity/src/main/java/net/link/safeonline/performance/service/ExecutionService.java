/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service;

import java.util.Date;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.bean.ExecutionServiceBean;


/**
 * <h2>{@link ExecutionServiceBean}<br>
 * <sub>Service bean for {@link ExecutionEntity}.</sub></h2>
 * 
 * <p>
 * Create {@link ExecutionEntity}s and manage the {@link ScenarioTimingEntity}s that they're linked with.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface ExecutionService {

    public static final String BINDING = "SafeOnline/ExecutionService";


    /**
     * Add an entry for a new scenario execution to the database.
     */
    public ExecutionEntity addExecution(String scenarioName, Integer agents, Integer workers, Date startTime,
            Long duration, String hostname, Boolean useSsl);

    /**
     * Retrieve an already created execution by its Id.
     */
    public ExecutionEntity getExecution(Date startTime);

    /**
     * Retrieve all driver profiles that were created for execution with the given Id.
     */
    public Set<DriverProfileEntity> getProfiles(Date startTime);

    /**
     * Signal a new start of a scenario in the given execution. A {@link ScenarioTimingEntity} will be created and
     * returned.
     */
    public ScenarioTimingEntity start(ExecutionEntity execution);

    /**
     * Retrieve all available execution IDs (which is the date at which they were initiated).
     */
    public Set<Date> getExecutions();

    /**
     * Force a recalculation of the speed in the given {@link ExecutionEntity}.
     */
    public void updateSpeed(ExecutionEntity execution);

}
