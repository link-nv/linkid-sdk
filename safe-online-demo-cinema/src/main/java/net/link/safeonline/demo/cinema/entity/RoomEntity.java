/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries( {
        @NamedQuery(name = RoomEntity.getById, query = "SELECT r FROM RoomEntity r WHERE r.id = :id"),
        @NamedQuery(name = RoomEntity.getFor, query = "SELECT r FROM RoomEntity r WHERE r.theatre = :theatre AND :film MEMBER OF r.films") })
public class RoomEntity implements Serializable {

    private static final long      serialVersionUID = 1L;
    public static final String     getFor           = "RoomEntity.getFor";
    public static final String     getById          = "RoomEntity.getById";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                   id;

    @ManyToMany(mappedBy = "rooms")
    private Collection<FilmEntity> films;

    @ManyToOne
    private TheatreEntity          theatre;
    private String                 name;


    public RoomEntity() {

    }

    public RoomEntity(String name, TheatreEntity theatre) {

        this.name = name;
        this.theatre = theatre;
    }

    /**
     * @return The theatre this room is in.
     */
    public TheatreEntity getTheatre() {

        return this.theatre;
    }

    /**
     * @return The name of this {@link RoomEntity}.
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return The films of this {@link RoomEntity}.
     */
    public Collection<FilmEntity> getFilms() {

        return this.films;
    }

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return this.id;
    }
}
