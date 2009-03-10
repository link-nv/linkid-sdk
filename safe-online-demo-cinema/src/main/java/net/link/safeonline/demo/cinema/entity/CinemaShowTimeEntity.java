/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * <h2>{@link CinemaShowTimeEntity}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Entity
@Table(name = "dCinemaShowTime")
public class CinemaShowTimeEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long              id;

    private Integer           monStart;
    private Integer           tueStart;
    private Integer           wedStart;
    private Integer           thuStart;
    private Integer           friStart;
    private Integer           satStart;
    private Integer           sunStart;


    public CinemaShowTimeEntity() {

    }

    public CinemaShowTimeEntity(Integer monStart, Integer tueStart, Integer wedStart, Integer thuStart, Integer friStart, Integer satStart,
                                Integer sunStart) {

        this.monStart = monStart;
        this.tueStart = tueStart;
        this.wedStart = wedStart;
        this.thuStart = thuStart;
        this.friStart = friStart;
        this.satStart = satStart;
        this.sunStart = sunStart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("[M: %s, T: %s, W: %s, T: %s, F: %s, S: %s, S: %s]", timeToStr(monStart), timeToStr(tueStart),
                timeToStr(wedStart), timeToStr(thuStart), timeToStr(friStart), timeToStr(satStart), timeToStr(sunStart));
    }

    private String timeToStr(Integer time) {

        if (time == null)
            return "-";

        long hours = time % 3600;
        long minutes = (time - hours * 3600) % 60;

        return String.format("%d:%d", hours, minutes);
    }

    public Integer getMonStart() {

        return monStart;
    }

    public Integer getTueStart() {

        return tueStart;
    }

    public Integer getWedStart() {

        return wedStart;
    }

    public Integer getThuStart() {

        return thuStart;
    }

    public Integer getFriStart() {

        return friStart;
    }

    public Integer getSatStart() {

        return satStart;
    }

    public Integer getSunStart() {

        return sunStart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CinemaShowTimeEntity clone() {

        return new CinemaShowTimeEntity(monStart, tueStart, wedStart, thuStart, friStart, satStart, sunStart);
    }
}
