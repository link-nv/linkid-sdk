/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


/**
 * <h2>{@link ScenarioTimingEntity}<br>
 * <sub>Holds the startTime at which a scenario has been executed.</sub></h2>
 * 
 * <p>
 * <i>Jan 17, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Entity
@NamedQueries( {
        @NamedQuery(name = ScenarioTimingEntity.getExecutionStart, query = "SELECT MIN(e.startTime)" + "    FROM ScenarioTimingEntity e"
                + "    WHERE e.execution = :execution"),
        @NamedQuery(name = ScenarioTimingEntity.getExecutionDuration, query = "SELECT MAX(e.startTime) - MIN(e.startTime)"
                + "    FROM ScenarioTimingEntity e" + "    WHERE e.execution = :execution"),
        @NamedQuery(name = ScenarioTimingEntity.getByExecution, query = "SELECT t" + "    FROM ScenarioTimingEntity t"
                + "    WHERE t.execution = :execution" + "    ORDER BY t.startTime"),
        @NamedQuery(name = ScenarioTimingEntity.createAverage, query = "SELECT NEW net.link.safeonline.performance.entity.ScenarioTimingEntity("
                + "        t.execution, MIN(t.startTime), AVG(t.olasDuration), AVG(t.agentDuration), AVG(t.startFreeMem), AVG(t.endFreeMem)"
                + "    )"
                + "    FROM ScenarioTimingEntity t"
                + "    WHERE t.execution = :execution"
                + "        AND t.startTime >= :start"
                + "        AND t.startTime < :stop" + "    GROUP BY t.execution") })
public class ScenarioTimingEntity implements Comparable<ScenarioTimingEntity> {

    public static final String getByExecution       = "ScenarioTimingEntity.getTimings";
    public static final String createAverage        = "ScenarioTimingEntity.createAverage";
    public static final String getExecutionStart    = "ScenarioTimingEntity.getExecutionStart";
    public static final String getExecutionDuration = "ScenarioTimingEntity.getExecutionDuration";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int                id;

    private Long               startTime;
    private Long               olasDuration;
    private Long               agentDuration;
    private Long               startFreeMem;
    private Long               endFreeMem;

    @ManyToOne
    private ExecutionEntity    execution;


    public ScenarioTimingEntity() {

        agentDuration = 0l;
        startTime = System.currentTimeMillis();
    }

    public ScenarioTimingEntity(ExecutionEntity execution) {

        this();

        this.execution = execution;
    }

    public ScenarioTimingEntity(ExecutionEntity execution, Long startTime, Double olasDuration, Double agentDuration, Double startFreeMem,
                                Double endFreeMem) {

        this.execution = execution;
        this.startTime = startTime;
        this.olasDuration = olasDuration == null? 0: olasDuration.longValue();
        this.agentDuration = agentDuration == null? 0: agentDuration.longValue();
        this.startFreeMem = startFreeMem == null? 0: startFreeMem.longValue();
        this.endFreeMem = endFreeMem == null? 0: endFreeMem.longValue();
    }

    /**
     * @return The startTime of this {@link ScenarioTimingEntity}.
     */
    public Long getStart() {

        return startTime;
    }

    /**
     * Add a new timing information about a call made to OLAS during the scenario that is timed with this entity.
     */
    public void addOlasTime(long newOlasTime) {

        if (olasDuration == null) {
            olasDuration = newOlasTime;
        } else {
            olasDuration += newOlasTime;
        }
    }

    /**
     * @return The duration of this {@link ScenarioTimingEntity}.
     */
    public Long getOlasDuration() {

        return olasDuration;
    }

    /**
     * @return The duration of this {@link ScenarioTimingEntity}.
     */
    public Long getAgentDuration() {

        return agentDuration;
    }

    /**
     * Signal that the scenario started at the startTime contained in this entity has just ended.
     */
    public void stop() {

        agentDuration = System.currentTimeMillis() - startTime;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(ScenarioTimingEntity o) {

        return startTime.compareTo(o.startTime);
    }

    /**
     * Remember the amount of memory was available when the execution of the scenario timed by this entity started.
     */
    public void setStartMemory(long startFreeMem) {

        this.startFreeMem = startFreeMem;
    }

    /**
     * @return The amount of memory was available when the execution of the scenario timed by this entity started.
     */
    public Long getStartFreeMem() {

        return startFreeMem;
    }

    /**
     * Remember the amount of memory was available when the execution of the scenario timed by this entity ended.
     */
    public void setEndMemory(long endFreeMem) {

        this.endFreeMem = endFreeMem;
    }

    /**
     * @return The amount of memory was available when the execution of the scenario timed by this entity ended.
     */
    public Long getEndFreeMem() {

        return endFreeMem;
    }

    /**
     * @return The execution of this {@link ScenarioTimingEntity}.
     */
    public ExecutionEntity getExecution() {

        return execution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("ST: %01.2f(%+01.2f) MB, %d(+{o:%d, a:%d})", startFreeMem / (1024 * 1024f),
                (endFreeMem - startFreeMem) / (1024 * 1024f), startTime, olasDuration, agentDuration);
    }
}
