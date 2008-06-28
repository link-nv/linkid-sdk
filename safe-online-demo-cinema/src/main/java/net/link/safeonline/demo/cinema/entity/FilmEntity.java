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
        @NamedQuery(name = FilmEntity.getAll, query = "SELECT f FROM FilmEntity f"),
        @NamedQuery(name = FilmEntity.getById, query = "SELECT f FROM FilmEntity f WHERE f.id = :id"),
        @NamedQuery(name = FilmEntity.getAllFrom, query = "SELECT r.films FROM RoomEntity r WHERE r.theatre = :theatre") })
public class FilmEntity implements Serializable {

    private static final long          serialVersionUID = 1L;
    public static final String         getAll           = "FilmEntity.getAll";
    public static final String         getById          = "FilmEntity.getById";
    public static final String         getAllFrom       = "FilmEntity.getAllFrom";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                       id;

    @ManyToMany
    private Collection<RoomEntity>     rooms;
    private String                     name;
    private String                     description;
    private long                       duration;
    private int                        price;

    @OneToMany(fetch = FetchType.EAGER)
    private Collection<ShowTimeEntity> times;
    private Date                       endTime;
    private Date                       startTime;


    public FilmEntity() {

        this.rooms = new HashSet<RoomEntity>();
        this.times = new HashSet<ShowTimeEntity>();
    }

    public FilmEntity(String name, String description, long duration,
            int price, Collection<ShowTimeEntity> times,
            Collection<RoomEntity> rooms) {

        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.times = times;
        this.rooms = rooms;
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
     * @return The duration of this {@link FilmEntity}.
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
    public Collection<RoomEntity> getRooms() {

        return this.rooms;
    }

    /**
     * @return The {@link ShowTimeEntity}s of this film.
     */
    public Collection<ShowTimeEntity> getTimes() {

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
