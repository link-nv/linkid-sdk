/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "dCinemaRoom")
@NamedQueries( {
        @NamedQuery(name = CinemaRoomEntity.getById, query = "SELECT r FROM CinemaRoomEntity r WHERE r.id = :id"),
        @NamedQuery(name = CinemaRoomEntity.getFor, query = "SELECT r FROM CinemaRoomEntity r WHERE r.theatre = :theatre AND :film MEMBER OF r.films") })
public class CinemaRoomEntity implements Serializable {

    private static final long            serialVersionUID = 1L;
    public static final String           getFor           = "CinemaRoomEntity.getFor";
    public static final String           getById          = "CinemaRoomEntity.getById";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                         id;

    @ManyToMany
    private Collection<CinemaFilmEntity> films;

    @ManyToOne
    private CinemaTheatreEntity          theatre;
    private String                       name;


    public CinemaRoomEntity() {

        films = new HashSet<CinemaFilmEntity>();
    }

    public CinemaRoomEntity(String name, CinemaTheatreEntity theatre) {

        this();

        this.name = name;
        this.theatre = theatre;
    }

    /**
     * @return The theatre this room is in.
     */
    public CinemaTheatreEntity getTheatre() {

        return theatre;
    }

    /**
     * @return The name of this {@link CinemaRoomEntity}.
     */
    public String getName() {

        return name;
    }

    /**
     * @return The films of this {@link CinemaRoomEntity}.
     */
    public Collection<CinemaFilmEntity> getFilms() {

        return films;
    }

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return id;
    }
}
