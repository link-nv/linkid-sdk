/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


/**
 * <h2>{@link ProfileDataEntity}<br>
 * <sub>Holds data gathered by OLAS during a single request.</sub></h2>
 * 
 * <p>
 * <i>Jan 10, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Entity
@NamedQueries( {
        @NamedQuery(name = ProfileDataEntity.getByProfile, query = "SELECT d" + "    FROM ProfileDataEntity d"
                + "    WHERE d.profile = :profile" + "    ORDER BY d.scenarioTiming.startTime"),
        @NamedQuery(name = ProfileDataEntity.getExecutionStart, query = "SELECT MIN(d.scenarioTiming.startTime)"
                + "    FROM ProfileDataEntity d            " + "    WHERE d.profile = :profile          "),
        @NamedQuery(name = ProfileDataEntity.getExecutionDuration, query = "SELECT MAX(d.scenarioTiming.startTime) - MIN(d.scenarioTiming.startTime)"
                + "    FROM ProfileDataEntity d            " + "    WHERE d.profile = :profile          "),
        @NamedQuery(name = ProfileDataEntity.createAverage, query = "SELECT NEW net.link.safeonline.performance.entity.MeasurementEntity("
                + "        m.measurement, AVG(m.duration)" + "    )                                 "
                + "    FROM ProfileDataEntity d          " + "        JOIN d.measurements m         "
                + "    WHERE d.profile = :profile        " + "        AND d.scenarioTiming.startTime >= :start      "
                + "        AND d.scenarioTiming.startTime < :stop      " + "    GROUP BY m.measurement            "),
        @NamedQuery(name = ProfileDataEntity.countByProfile, query = "SELECT COUNT(d)" + "    FROM ProfileDataEntity d"
                + "    WHERE d.profile = :profile" + "    ORDER BY d.scenarioTiming.startTime") })
public class ProfileDataEntity {

    public static final String     getByProfile         = "ProfileDataEntity.getByProfile";
    public static final String     createAverage        = "ProfileDataEntity.createAverage";
    public static final String     countByProfile       = "ProfileDataEntity.countByProfile";
    public static final String     getExecutionStart    = "ProfileDataEntity.getExecutionStart";
    public static final String     getExecutionDuration = "ProfileDataEntity.getExecutionDuration";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int                    id;

    @ManyToOne
    private DriverProfileEntity    profile;

    @OneToMany(mappedBy = "profileData")
    private Set<MeasurementEntity> measurements;

    @ManyToOne
    private ScenarioTimingEntity   scenarioTiming;


    public ProfileDataEntity() {

        this.measurements = new HashSet<MeasurementEntity>();
    }

    public ProfileDataEntity(DriverProfileEntity profile, ScenarioTimingEntity scenarioStart) {

        this();

        this.profile = profile;
        this.scenarioTiming = scenarioStart;
    }

    /**
     * @return The measurements of this {@link ProfileDataEntity}.
     */
    public Set<MeasurementEntity> getMeasurements() {

        return this.measurements;
    }

    /**
     * The time the scenario execution of this profile data was started.
     */
    public ScenarioTimingEntity getScenarioTiming() {

        return this.scenarioTiming;
    }

    /**
     * @return The {@link DriverProfileEntity} that generated this {@link ProfileDataEntity}.
     */
    public DriverProfileEntity getProfile() {

        return this.profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("[PD: %s - ST: %s]", this.measurements, this.scenarioTiming);
    }
}
