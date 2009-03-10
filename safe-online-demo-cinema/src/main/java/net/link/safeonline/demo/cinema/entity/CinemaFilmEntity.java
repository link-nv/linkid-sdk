/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "dCinemaFilm")
@NamedQueries( { @NamedQuery(name = CinemaFilmEntity.getAll, query = "SELECT f FROM CinemaFilmEntity f"),
        @NamedQuery(name = CinemaFilmEntity.getById, query = "SELECT f FROM CinemaFilmEntity f WHERE f.id = :id"),
        @NamedQuery(name = CinemaFilmEntity.getAllFrom, query = "SELECT r.films FROM CinemaRoomEntity r WHERE r.theatre = :theatre") })
public class CinemaFilmEntity implements Serializable {

    private static final long                serialVersionUID = 1L;
    public static final String               getAll           = "CinemaFilmEntity.getAll";
    public static final String               getById          = "CinemaFilmEntity.getById";
    public static final String               getAllFrom       = "CinemaFilmEntity.getAllFrom";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                             id;

    private String                           name;
    private String                           description;
    private long                             duration;
    private long                             price;

    @OneToMany(fetch = FetchType.EAGER)
    private Collection<CinemaShowTimeEntity> times;
    private Date                             endTime;
    private Date                             startTime;


    public CinemaFilmEntity() {

        times = new HashSet<CinemaShowTimeEntity>();
    }

    /**
     * @param duration
     *            The duration of the film in seconds.
     */
    public CinemaFilmEntity(String name, String description, long duration, long price, Collection<CinemaShowTimeEntity> times) {

        this.name = name;
        this.description = description;
        this.duration = duration * 1000;
        this.price = price;
        this.times = times;
    }

    /**
     * @return The name of this {@link CinemaFilmEntity}
     */
    public String getName() {

        return name;
    }

    /**
     * @return The description of this {@link CinemaFilmEntity}.
     */
    public String getDescription() {

        return description;
    }

    /**
     * @return The duration of this {@link CinemaFilmEntity}.
     */
    public long getDuration() {

        return duration;
    }

    /**
     * @return The base price for one viewing of this film.
     */
    public long getPrice() {

        return price;
    }

    /**
     * @return The {@link CinemaShowTimeEntity}s of this film.
     */
    public Collection<CinemaShowTimeEntity> getTimes() {

        return times;
    }

    /**
     * @return The date of this film's premiere.
     */
    public Date getStartTime() {

        return startTime;
    }

    /**
     * @return The date of the last showing of this film.
     */
    public Date getEndTime() {

        return endTime;
    }

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return id;
    }
}
