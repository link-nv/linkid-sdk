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

@Entity
@NamedQueries( {
        @NamedQuery(name = TheatreEntity.getAll, query = "SELECT t FROM TheatreEntity t"),
        @NamedQuery(name = TheatreEntity.getById, query = "SELECT t FROM TheatreEntity t WHERE t.id = :id"),
        @NamedQuery(name = TheatreEntity.getAllFor, query = "SELECT r.theatre FROM RoomEntity r WHERE :film MEMBER OF r.films") })
public class TheatreEntity implements Serializable {

    private static final long  serialVersionUID = 1L;
    public static final String getAll           = "TheatreEntity.getAll";
    public static final String getById          = "TheatreEntity.getById";
    public static final String getAllFor        = "TheatreEntity.getAllFor";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long               id;

    private String             name;
    private String             address;


    public TheatreEntity() {

    }

    public TheatreEntity(String name, String address) {

        this.name = name;
        this.address = address;
    }

    /**
     * @return The name of this {@link TheatreEntity}
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return The address of this {@link TheatreEntity}.
     */
    public String getAddress() {

        return this.address;
    }

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return this.id;
    }
}
