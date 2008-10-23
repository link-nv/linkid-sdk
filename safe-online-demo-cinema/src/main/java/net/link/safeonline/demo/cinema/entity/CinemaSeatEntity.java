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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@Entity
@NamedQueries( {

@NamedQuery(name = CinemaSeatEntity.getById, query = "SELECT s FROM CinemaSeatEntity s WHERE s.id = :id"),
        @NamedQuery(name = CinemaSeatEntity.getFor, query = "SELECT s FROM CinemaSeatEntity s WHERE s.room = :room") })
public class CinemaSeatEntity implements Serializable {

    private static final long  serialVersionUID = 1L;
    public static final String getById          = "CinemaSeatEntity.getById";
    public static final String getFor           = "CinemaSeatEntity.getFor";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long               id;

    @ManyToOne
    private CinemaRoomEntity   room;
    private int                x;
    private int                y;


    public CinemaSeatEntity() {

    }

    public CinemaSeatEntity(CinemaRoomEntity room, int x, int y) {

        this.room = room;
        this.x = x;
        this.y = y;
    }

    /**
     * @return The room this seat is in.
     */
    public CinemaRoomEntity getRoom() {

        return this.room;
    }

    /**
     * @return The horizontal location of the seat in the room.
     */
    public int getX() {

        return this.x;
    }

    /**
     * @return The vertical location of the seat in the room.
     */
    public int getY() {

        return this.y;
    }

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CinemaSeatEntity))
            return false;
        if (obj == this)
            return true;

        CinemaSeatEntity other = (CinemaSeatEntity) obj;
        return this.x == other.x && this.y == other.y && this.room.getId() == other.room.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("{id: %d - room %d: %d, %d}", this.id, this.room.getId(), this.x, this.y);
    }
}
