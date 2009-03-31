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
import javax.persistence.Table;


@Entity
@Table(name = "dCinemaSeat")
@NamedQueries( { @NamedQuery(name = CinemaSeatEntity.getById, query = "SELECT s FROM CinemaSeatEntity s WHERE s.id = :id"),
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

        return room;
    }

    /**
     * @return The horizontal location of the seat in the room.
     */
    public int getX() {

        return x;
    }

    /**
     * @return The vertical location of the seat in the room.
     */
    public int getY() {

        return y;
    }

    /**
     * @return The unique identifier.
     */
    public long getId() {

        return id;
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
        return x == other.x && y == other.y && room.getId() == other.room.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return (int) (1 * x + 2 * y + 3 * room.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("{id: %d - room %d: %d, %d}", id, room.getId(), x, y);
    }
}
