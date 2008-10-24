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
 * <h2>{@link DriverExceptionEntity}<br>
 * <sub>Holds problems encountered during driver execution.</sub></h2>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Entity
@NamedQueries( {
        @NamedQuery(name = DriverExceptionEntity.getByProfile, query = "SELECT e" + "    FROM DriverExceptionEntity e"
                + "    WHERE e.profile = :profile"),
        @NamedQuery(name = DriverExceptionEntity.getExecutionStart, query = "SELECT MIN(e.occurredTime)"
                + "    FROM DriverExceptionEntity e            " + "    WHERE e.profile = :profile          "),
        @NamedQuery(name = DriverExceptionEntity.getExecutionDuration, query = "SELECT MAX(e.occurredTime) - MIN(e.occurredTime)"
                + "    FROM DriverExceptionEntity e            " + "    WHERE e.profile = :profile          "),
        @NamedQuery(name = DriverExceptionEntity.createAverage, query = "SELECT NEW net.link.safeonline.performance.entity.DriverExceptionEntity("
                + "        e.profile, AVG(e.occurredTime), e.message"
                + "    )                                 "
                + "    FROM DriverExceptionEntity e          "
                + "    WHERE e.profile = :profile        "
                + "        AND e.occurredTime >= :start      "
                + "        AND e.occurredTime < :stop      "
                + "    GROUP BY e.message            ") })
public class DriverExceptionEntity {

    public static final String  getByProfile         = "DriverExceptionEntity.getByProfile";
    public static final String  createAverage        = "DriverExceptionEntity.createAverage";
    public static final String  getExecutionStart    = "DriverExceptionEntity.getExecutionStart";
    public static final String  getExecutionDuration = "DriverExceptionEntity.getExecutionDuration";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int                 id;

    @ManyToOne
    private DriverProfileEntity profile;

    private long                occurredTime;
    private String              message;


    public DriverExceptionEntity() {

    }

    public DriverExceptionEntity(DriverProfileEntity profile, Double occurredTime, String message) {

        this(profile, occurredTime.longValue(), message);
    }

    public DriverExceptionEntity(DriverProfileEntity profile, long occurredTime, String message) {

        this.profile = profile;
        this.occurredTime = occurredTime;
        this.message = message;
    }

    /**
     * @return The {@link DriverProfileEntity} that generated this {@link DriverExceptionEntity}.
     */
    public DriverProfileEntity getProfile() {

        return this.profile;
    }

    /**
     * @return The time the exception occurred.
     */
    public long getOccurredTime() {

        return this.occurredTime;
    }

    /**
     * @return A message describing problem that occurred.
     */
    public String getMessage() {

        return this.message;
    }
}
