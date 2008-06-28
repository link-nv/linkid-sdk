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

        @NamedQuery(name = SeatEntity.getById, query = "SELECT s FROM SeatEntity s WHERE s.id = :id"),
        @NamedQuery(name = SeatEntity.getFor, query = "SELECT s FROM SeatEntity s WHERE s.room = :room") })
public class SeatEntity implements Serializable {

    private static final long  serialVersionUID = 1L;
    public static final String getById          = "SeatEntity.getById";
    public static final String getFor           = "SeatEntity.getFor";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long               id;

    @ManyToOne
    private RoomEntity         room;
    private int                x;
    private int                y;


    public SeatEntity() {

    }

    public SeatEntity(RoomEntity room, int x, int y) {

        this.room = room;
        this.x = x;
        this.y = y;
    }

    /**
     * @return The room this seat is in.
     */
    public RoomEntity getRoom() {

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

        if (!(obj instanceof SeatEntity))
            return false;
        if (obj == this)
            return true;

        SeatEntity other = (SeatEntity) obj;
        return this.x == other.x && this.y == other.y
                && this.room.getId() == other.room.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("{id: %d - room %d: %d, %d}", this.id, this.room
                .getId(), this.x, this.y);
    }
}
