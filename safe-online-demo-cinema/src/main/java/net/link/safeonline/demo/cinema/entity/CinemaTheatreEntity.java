/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "dCinemaTheatre")
@NamedQueries( { @NamedQuery(name = CinemaTheatreEntity.getAll, query = "SELECT t FROM CinemaTheatreEntity t"),
        @NamedQuery(name = CinemaTheatreEntity.getById, query = "SELECT t FROM CinemaTheatreEntity t WHERE t.id = :id"),
        @NamedQuery(name = CinemaTheatreEntity.getAllFor, query = "SELECT r.theatre FROM CinemaRoomEntity r WHERE :film MEMBER OF r.films") })
public class CinemaTheatreEntity implements Serializable {

    private static final long  serialVersionUID = 1L;
    public static final String getAll           = "CinemaTheatreEntity.getAll";
    public static final String getById          = "CinemaTheatreEntity.getById";
    public static final String getAllFor        = "CinemaTheatreEntity.getAllFor";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long               id;

    private String             name;
    private String             address;


    public CinemaTheatreEntity() {

    }

    public CinemaTheatreEntity(String name, String address) {

        this.name = name;
        this.address = address;
    }

    /**
     * @return The name of this {@link CinemaTheatreEntity}
     */
    public String getName() {

        return name;
    }

    /**
     * @return The address of this {@link CinemaTheatreEntity}.
     */
    public String getAddress() {

        return address;
    }

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return id;
    }
}
