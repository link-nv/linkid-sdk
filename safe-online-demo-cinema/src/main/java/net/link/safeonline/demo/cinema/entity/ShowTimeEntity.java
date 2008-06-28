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

/**
 * <h2>{@link ShowTimeEntity}<br>
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
public class ShowTimeEntity implements Serializable {

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


    public ShowTimeEntity() {

    }

    public ShowTimeEntity(Integer monStart, Integer tueStart, Integer wedStart,
            Integer thuStart, Integer friStart, Integer satStart,
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

        return String.format(
                "[M: %s, T: %s, W: %s, T: %s, F: %s, S: %s, S: %s]",
                timeToStr(this.monStart), timeToStr(this.tueStart),
                timeToStr(this.wedStart), timeToStr(this.thuStart),
                timeToStr(this.friStart), timeToStr(this.satStart),
                timeToStr(this.sunStart));
    }

    private String timeToStr(Integer time) {

        if (time == null)
            return "-";

        long hours = time % 3600;
        long minutes = (time - hours * 3600) % 60;

        return String.format("%d:%d", hours, minutes);
    }

    public Integer getMonStart() {

        return this.monStart;
    }

    public Integer getTueStart() {

        return this.tueStart;
    }

    public Integer getWedStart() {

        return this.wedStart;
    }

    public Integer getThuStart() {

        return this.thuStart;
    }

    public Integer getFriStart() {

        return this.friStart;
    }

    public Integer getSatStart() {

        return this.satStart;
    }

    public Integer getSunStart() {

        return this.sunStart;
    }
}
