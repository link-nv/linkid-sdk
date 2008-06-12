/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class FilmEntity {

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                id;

    private String              name;
    private String              description;
    private int                 price;

    @OneToMany
    private Set<ShowTimeEntity> times;
    private Date                endTime;
    private Date                startTime;


    public FilmEntity(String name, String description, int price,
            Set<ShowTimeEntity> times) {

        this.name = name;
        this.description = description;
        this.price = price;
        this.times = times;
    }

    /**
     * @return The name of this {@link FilmEntity}
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return The description of this {@link FilmEntity}.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * @return The base price for one viewing of this film.
     */
    public int getPrice() {

        return this.price;
    }

    /**
     * @return The {@link ShowTimeEntity}s of this film.
     */
    public Set<ShowTimeEntity> getTimes() {

        return this.times;
    }

    /**
     * @return The date of this film's premiere.
     */
    public Date getStartTime() {

        return this.startTime;
    }

    /**
     * @return The date of the last showing of this film.
     */
    public Date getEndTime() {

        return this.endTime;
    }
}
