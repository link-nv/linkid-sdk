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
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


@Entity
@NamedQueries( {
        @NamedQuery(name = CinemaFilmEntity.getAll, query = "SELECT f FROM CinemaFilmEntity f"),
        @NamedQuery(name = CinemaFilmEntity.getById, query = "SELECT f FROM CinemaFilmEntity f WHERE f.id = :id"),
        @NamedQuery(name = CinemaFilmEntity.getAllFrom, query = "SELECT r.films FROM CinemaRoomEntity r WHERE r.theatre = :theatre") })
public class CinemaFilmEntity implements Serializable {

    private static final long          serialVersionUID = 1L;
    public static final String         getAll           = "CinemaFilmEntity.getAll";
    public static final String         getById          = "CinemaFilmEntity.getById";
    public static final String         getAllFrom       = "CinemaFilmEntity.getAllFrom";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                       id;

    @ManyToMany
    private Collection<CinemaRoomEntity>     rooms;
    private String                     name;
    private String                     description;
    private long                       duration;
    private int                        price;

    @OneToMany(fetch = FetchType.EAGER)
    private Collection<CinemaShowTimeEntity> times;
    private Date                       endTime;
    private Date                       startTime;


    public CinemaFilmEntity() {

        this.rooms = new HashSet<CinemaRoomEntity>();
        this.times = new HashSet<CinemaShowTimeEntity>();
    }

    /**
     * @param duration
     *            The duration of the film in seconds.
     */
    public CinemaFilmEntity(String name, String description, long duration, int price, Collection<CinemaShowTimeEntity> times,
            Collection<CinemaRoomEntity> rooms) {

        this.name = name;
        this.description = description;
        this.duration = duration * 1000;
        this.price = price;
        this.times = times;
        this.rooms = rooms;
    }

    /**
     * @return The name of this {@link CinemaFilmEntity}
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return The description of this {@link CinemaFilmEntity}.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * @return The duration of this {@link CinemaFilmEntity}.
     */
    public long getDuration() {

        return this.duration;
    }

    /**
     * @return The base price for one viewing of this film.
     */
    public int getPrice() {

        return this.price;
    }

    /**
     * @return The rooms this film plays in.
     */
    public Collection<CinemaRoomEntity> getRooms() {

        return this.rooms;
    }

    /**
     * @return The {@link CinemaShowTimeEntity}s of this film.
     */
    public Collection<CinemaShowTimeEntity> getTimes() {

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

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return this.id;
    }
}
