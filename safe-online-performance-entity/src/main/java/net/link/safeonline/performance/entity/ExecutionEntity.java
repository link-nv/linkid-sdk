/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import net.link.safeonline.performance.service.ExecutionService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link ExecutionEntity}<br>
 * <sub>Holds the global metadata for a scenario execution.</sub></h2>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
@NamedQueries( {
        @NamedQuery(name = ExecutionEntity.findAll, query = "SELECT e" + "    FROM ExecutionEntity e"),
        @NamedQuery(name = ExecutionEntity.findById, query = "SELECT e" + "    FROM ExecutionEntity e"
                + "    WHERE e.startTime = :startTime"),
        @NamedQuery(name = ExecutionEntity.calcSpeed, query = "SELECT 1000d * COUNT(t) / ( MAX(t.startTime) - MIN(t.startTime) )"
                + "    FROM ScenarioTimingEntity t" + "    WHERE t.execution = :execution") })
public class ExecutionEntity {

    private static final Log         LOG       = LogFactory.getLog(ExecutionEntity.class);

    public static final String       findAll   = "ExecutionEntity.findAll";
    public static final String       findById  = "ExecutionEntity.findById";
    public static final String       calcSpeed = "ExecutionEntity.calcSpeed";

    @Id
    private Date                     startTime;

    private String                   scenarioName;
    private int                      agents;
    private int                      workers;
    private long                     duration;
    private String                   hostname;
    private Double                   speed;
    private boolean                  useSsl;
    private boolean                  dirtySpeed;
    private Double                   chartingProgress;

    @Lob
    @Column(length = 100 * 1024 * 1024, nullable = true)
    private byte[]                   charts;

    @OneToMany(mappedBy = "execution")
    private Set<DriverProfileEntity> profiles;


    public ExecutionEntity() {

        this.profiles = new TreeSet<DriverProfileEntity>();
        this.dirtySpeed = false;
    }

    public ExecutionEntity(String scenarioName, Integer agents, int workers, Date startTime, long duration,
            String hostname, Boolean ssl) {

        this();

        this.scenarioName = scenarioName;
        this.agents = agents;
        this.workers = workers;
        this.startTime = startTime;
        this.duration = duration;
        this.hostname = hostname;
        this.useSsl = ssl;
    }

    /**
     * @return The chartingProgress of this {@link ExecutionEntity}.
     */
    public Double getChartingProgress() {

        return this.chartingProgress;
    }

    /**
     * @param chartingProgress
     *            The chartingProgress of this {@link ExecutionEntity}.
     */
    public void setChartingProgress(Double chartingProgress) {

        this.chartingProgress = chartingProgress;
    }

    /**
     * @return The charts of this {@link ExecutionEntity}.
     */
    @SuppressWarnings("unchecked")
    public Map<String, byte[][]> getCharts() {

        if (this.charts == null)
            return null;

        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(this.charts);
            ObjectInputStream deserializer = new ObjectInputStream(bytes);

            return (Map<String, byte[][]>) deserializer.readObject();
        }

        catch (IOException e) {
            LOG.error("Couldn't deserialize charts!", e);

        } catch (ClassNotFoundException e) {
            LOG.error("Charts object is in a class that could not be resolved!", e);
        }

        return null;
    }

    /**
     * @param charts
     *            The charts of this {@link ExecutionEntity}.
     */
    public void setCharts(Map<String, byte[][]> charts) {

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream serializer = new ObjectOutputStream(bytes);
            serializer.writeObject(charts);

            this.charts = bytes.toByteArray();
        }

        catch (IOException e) {
            LOG.error("Couldn't serialize charts!", e);
        }
    }

    /**
     * @return The name of the scenario that was executed.
     */
    public String getScenarioName() {

        return this.scenarioName;
    }

    /**
     * @return The name of the host that runs the OLAS service.
     */
    public String getHostname() {

        return this.hostname;
    }

    /**
     * @return <code>true</code> If we want to use SSL for communication with OLAS.
     */
    public Boolean isSsl() {

        return this.useSsl;
    }

    /**
     * @return The driver profiles generated for this execution.
     */
    public Set<DriverProfileEntity> getProfiles() {

        return this.profiles;
    }

    /**
     * @return The amount of agents this scenario execution was initiated on.
     */
    public int getAgents() {

        return this.agents;
    }

    /**
     * @return The amount of workers that was used to process this execution.
     */
    public int getWorkers() {

        return this.workers;
    }

    /**
     * @return The time at which this execution first started.
     */
    public Date getStartTime() {

        return this.startTime;
    }

    /**
     * @return The amount of time this execution was schedules to run (ms).
     */
    public long getDuration() {

        return this.duration;
    }

    /**
     * The speed will only be recalculated if it has been set as dirty (which automatically happens each time a scenario
     * has been completed for it).
     *
     * @return The average scenario execution speed (#/s) in this execution.
     */
    public Double getSpeed() {

        if (this.dirtySpeed || this.speed == null) {
            try {
                ((ExecutionService) new InitialContext().lookup(ExecutionService.BINDING)).updateSpeed(this);
            } catch (NamingException e) {
            }
        }

        return this.speed;
    }

    /**
     * @param speed
     *            The average scenario execution speed in this execution.
     */
    public void setSpeed(Double speed) {

        this.speed = speed;
        this.dirtySpeed = false;
    }

    /**
     * Signal that the speed value currently contained in this {@link ExecutionEntity} is dirty and needs to be
     * recalculated.
     */
    public void dirtySpeed() {

        this.dirtySpeed = true;
    }
}
