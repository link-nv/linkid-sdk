/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@Entity
// @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "seat", "start"
// }))
@NamedQueries( { @NamedQuery(name = CinemaSeatOccupationEntity.getFor, query = "SELECT o FROM CinemaSeatOccupationEntity o WHERE o.seat = :seat AND o.start = :start") })
public class CinemaSeatOccupationEntity implements Serializable {

    private static final long  serialVersionUID = 1L;
    public static final String getFor           = "CinemaSeatOccupationEntity.getFor";

    @Id
    @SuppressWarnings("unused")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long               id;

    @ManyToOne
    private CinemaSeatEntity         seat;
    private Date               start;
    private boolean            reserved;


    public CinemaSeatOccupationEntity() {

    }

    public CinemaSeatOccupationEntity(CinemaSeatEntity seat, Date start) {

        this.seat = seat;
        this.start = start;
        this.reserved = false;
    }

    /**
     * @return The seat that this entity occupies.
     */
    public CinemaSeatEntity getSeat() {

        return this.seat;
    }

    /**
     * @return The time the seat occupation begins.
     */
    public Date getStart() {

        return this.start;
    }

    /**
     * @return <code>true</code> if this seat occupation can no longer be taken by anyone else.
     */
    public boolean isReserved() {

        return this.reserved;
    }

    /**
     * Make sure this seat occupation can no longer be taken by anyone else.
     *
     * @throws IllegalStateException
     *             If the seat occupation has already been reserved.
     */
    public void reserve() throws IllegalStateException {

        if (this.reserved) {
            throw new IllegalStateException("Seat " + this.seat + " is already reserved!");
        }

        this.reserved = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return String.format("{Occ: %s - %s}", this.seat, DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(this.start));
    }
}
