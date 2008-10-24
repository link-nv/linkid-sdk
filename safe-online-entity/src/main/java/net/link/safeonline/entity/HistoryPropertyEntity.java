/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.HistoryPropertyEntity.QUERY_WHERE_HISTORY;

import java.io.Serializable;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "history_property")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_HISTORY, query = "SELECT historyProp " + "FROM HistoryPropertyEntity AS historyProp "
        + "WHERE historyProp.history = :history") })
public class HistoryPropertyEntity implements Serializable {

    private static final long  serialVersionUID          = 1L;

    public static final String QUERY_WHERE_HISTORY       = "hp.his";

    public static final String ID_COLUMN_NAME            = "id";

    public static final String PROPERTY_NAME_COLUMN_NAME = "name";

    private HistoryPropertyPK  pk;

    private HistoryEntity      history;

    private String             name;

    private String             value;


    public HistoryPropertyEntity() {

        // empty
    }

    public HistoryPropertyEntity(HistoryEntity history, String name, String value) {

        this.history = history;
        this.name = name;
        this.value = value;
        this.pk = new HistoryPropertyPK(history.getId(), name);
    }

    @Column(name = PROPERTY_NAME_COLUMN_NAME, insertable = false, updatable = false)
    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getValue() {

        return this.value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    @EmbeddedId
    @AttributeOverrides( { @AttributeOverride(name = "id", column = @Column(name = ID_COLUMN_NAME)),
            @AttributeOverride(name = "name", column = @Column(name = PROPERTY_NAME_COLUMN_NAME)) })
    public HistoryPropertyPK getPk() {

        return this.pk;
    }

    public void setPk(HistoryPropertyPK pk) {

        this.pk = pk;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = ID_COLUMN_NAME, insertable = false, updatable = false)
    public HistoryEntity getHistory() {

        return this.history;
    }

    public void setHistory(HistoryEntity history) {

        this.history = history;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof HistoryPropertyEntity)
            return false;
        HistoryPropertyEntity rhs = (HistoryPropertyEntity) obj;
        return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", this.pk).append("value", this.value).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_HISTORY)
        List<HistoryPropertyEntity> listProperties(@QueryParam("history") HistoryEntity history);
    }

}
