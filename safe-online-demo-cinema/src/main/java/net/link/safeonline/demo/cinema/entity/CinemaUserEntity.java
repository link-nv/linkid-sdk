/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@Entity
@NamedQueries( { @NamedQuery(name = CinemaUserEntity.getByOlasId, query = "SELECT u FROM CinemaUserEntity u WHERE u.olasId = :olasId") })
public class CinemaUserEntity implements Serializable {

    private static final long  serialVersionUID = 1L;

    public static final String getByOlasId      = "CinemaUserEntity.getByOlasId";

    @Id
    private String             olasId;

    private boolean            junior;

    private String             nrn;

    private String             name;


    public CinemaUserEntity() {

    }

    public CinemaUserEntity(String id) {

        this.olasId = id;
    }

    /**
     * @return The OLAS mapped olasId of the user for this application.
     */
    public String getOlasId() {

        return this.olasId;
    }

    /**
     * @param isJunior
     *            <code>true</code> if this user has a junior account.
     */
    public void setJunior(boolean isJunior) {

        this.junior = isJunior;
    }

    /**
     * @return <code>true</code> if this user has a junior account.
     */
    public boolean isJunior() {

        return this.junior;
    }

    /**
     * @param nrn
     *            The user's national registry number.
     */
    public void setNrn(String nrn) {

        this.nrn = nrn;
    }

    /**
     * @return The user's national registry number.
     */
    public String getNrn() {

        return this.nrn;
    }

    /**
     * @param name
     *            The OLAS username of this user.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * @return The OLAS username of this user.
     */
    public String getName() {

        return this.name;
    }
}
