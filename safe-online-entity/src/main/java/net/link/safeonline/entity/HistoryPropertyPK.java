/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Embeddable
public class HistoryPropertyPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private long              id;

    private String            name;


    public HistoryPropertyPK() {

        // empty
    }

    public HistoryPropertyPK(long id, String name) {

        this.id = id;
        this.name = name;
    }

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof HistoryPropertyPK)
            return false;
        HistoryPropertyPK rhs = (HistoryPropertyPK) obj;
        return new EqualsBuilder().append(id, rhs.id).append(name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(id).append(name).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("id", id).append("name", name).toString();
    }
}
