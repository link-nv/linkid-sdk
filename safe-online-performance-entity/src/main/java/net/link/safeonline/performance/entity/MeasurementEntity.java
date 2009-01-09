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


/**
 * <h2>{@link MeasurementEntity}<br>
 * <sub>Holds a description and duration for a single measurement.</sub></h2>
 * 
 * <p>
 * <i>Jan 14, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Entity
public class MeasurementEntity {

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int               id;

    @ManyToOne
    private ProfileDataEntity profileData;

    private String            measurement;
    private Long              duration;


    public MeasurementEntity() {

    }

    public MeasurementEntity(String measurement, Double duration) {

        this(null, measurement, duration.longValue());
    }

    public MeasurementEntity(ProfileDataEntity profileData, String measurement, Long duration) {

        this.profileData = profileData;
        this.measurement = measurement;
        this.duration = duration;
    }

    /**
     * @return The measurement of this {@link MeasurementEntity}.
     */
    public String getMeasurement() {

        return measurement;
    }

    /**
     * @return The duration of this {@link MeasurementEntity}.
     */
    public Long getDuration() {

        return duration;
    }

    /**
     * @return The {@link ProfileDataEntity} linking to this {@link MeasurementEntity}.
     */
    public ProfileDataEntity getProfileData() {

        return profileData;
    }

    /**
     * Only to be used for when this entity was created using {@link #MeasurementEntity(String, Double)}.
     */
    public void setProfileData(ProfileDataEntity profileData) {

        this.profileData = profileData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("ME: %s=%s", measurement, duration);
    }
}
