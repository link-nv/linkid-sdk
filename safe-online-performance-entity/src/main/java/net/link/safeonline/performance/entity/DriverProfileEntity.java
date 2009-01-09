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
import javax.persistence.NamedQuery;


/**
 * <h2>{@link DriverProfileEntity}<br>
 * <sub>Links the {@link ProfileDataEntity}s that belong to a certain driver in a certain execution.</sub></h2>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Entity
@NamedQuery(name = DriverProfileEntity.findByExecution, query = "SELECT p" + "    FROM DriverProfileEntity p"
        + "    WHERE p.driverClassName = :driverClassName AND p.execution = :execution")
public class DriverProfileEntity implements Comparable<DriverProfileEntity> {

    public static final String findByExecution = "DriverProfileEntity.findByExecution";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int                id;

    private String             driverClassName;

    @ManyToOne
    private ExecutionEntity    execution;


    public DriverProfileEntity() {

    }

    public DriverProfileEntity(String driverClassName, ExecutionEntity execution) {

        this.driverClassName = driverClassName;
        this.execution = execution;
    }

    /**
     * @return The name of the driver class that this profile applies to.
     */
    public String getDriverClassName() {

        return driverClassName;
    }

    /**
     * @return The execution of this {@link DriverProfileEntity}.
     */
    public ExecutionEntity getExecution() {

        return execution;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(DriverProfileEntity o) {

        return driverClassName.compareTo(o.driverClassName);
    }
}
