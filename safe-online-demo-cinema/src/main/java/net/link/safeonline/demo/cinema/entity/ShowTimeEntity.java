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

    private Long              monStart;
    private Long              tueStart;
    private Long              wedStart;
    private Long              thuStart;
    private Long              friStart;
    private Long              satStart;
    private Long              sunStart;


    public ShowTimeEntity(Long monStart, Long tueStart, Long wedStart,
            Long thuStart, Long friStart, Long satStart, Long sunStart) {

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

    private String timeToStr(Long time) {

        if (time == null)
            return "-";

        long hours = time % 3600;
        long minutes = (time - hours * 3600) % 60;

        return String.format("%d:%d", hours, minutes);
    }

    public Long getMonStart() {

        return this.monStart;
    }

    public Long getTueStart() {

        return this.tueStart;
    }

    public Long getWedStart() {

        return this.wedStart;
    }

    public Long getThuStart() {

        return this.thuStart;
    }

    public Long getFriStart() {

        return this.friStart;
    }

    public Long getSatStart() {

        return this.satStart;
    }

    public Long getSunStart() {

        return this.sunStart;
    }
}
