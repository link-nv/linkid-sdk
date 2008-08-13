/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.HistoryEntity.QUERY_DELETE_ALL;
import static net.link.safeonline.entity.HistoryEntity.QUERY_DELETE_WHERE_OLDER;
import static net.link.safeonline.entity.HistoryEntity.QUERY_WHERE_OLDER;
import static net.link.safeonline.entity.HistoryEntity.QUERY_WHERE_SUBJECT;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;


@Entity
@Table(name = "hist")
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT history " + "FROM HistoryEntity AS history "
                + "WHERE history.subject = :subject " + "ORDER BY history.when DESC"),
        @NamedQuery(name = QUERY_WHERE_OLDER, query = "SELECT history " + "FROM HistoryEntity AS history "
                + "WHERE history.when < :ageLimit "),
        @NamedQuery(name = QUERY_DELETE_WHERE_OLDER, query = "DELETE " + "FROM HistoryEntity AS history "
                + "WHERE history.when < :ageLimit"),
        @NamedQuery(name = QUERY_DELETE_ALL, query = "DELETE FROM HistoryEntity AS history "
                + "WHERE history.subject = :subject") })
public class HistoryEntity implements Serializable {

    public static final String                 QUERY_WHERE_SUBJECT      = "hist.subject";

    public static final String                 QUERY_WHERE_OLDER        = "hist.old";

    public static final String                 QUERY_DELETE_WHERE_OLDER = "hist.del.old";

    public static final String                 QUERY_DELETE_ALL         = "hist.del.all";

    private static final long                  serialVersionUID         = 1L;

    private long                               id;

    private SubjectEntity                      subject;

    private HistoryEventType                   event;

    private Map<String, HistoryPropertyEntity> properties;

    private Date                               when;


    public HistoryEntity() {

        // empty
    }

    public HistoryEntity(Date when, SubjectEntity subject, HistoryEventType event) {

        this.subject = subject;
        this.event = event;
        this.when = when;
        this.properties = new HashMap<String, HistoryPropertyEntity>();

    }

    @ManyToOne(optional = false)
    public SubjectEntity getSubject() {

        return this.subject;
    }

    public void setSubject(SubjectEntity subject) {

        this.subject = subject;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    @Column(name = "histevent", nullable = false)
    public HistoryEventType getEvent() {

        return this.event;
    }

    public void setEvent(HistoryEventType event) {

        this.event = event;
    }

    @Column(name = "whendate", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getWhen() {

        return this.when;
    }

    public void setWhen(Date when) {

        this.when = when;
    }

    @OneToMany(mappedBy = "history", fetch = FetchType.EAGER)
    @MapKey(name = "name")
    public Map<String, HistoryPropertyEntity> getProperties() {

        return this.properties;
    }

    public void setProperties(Map<String, HistoryPropertyEntity> properties) {

        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof HistoryEntity)
            return false;
        HistoryEntity rhs = (HistoryEntity) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_SUBJECT)
        List<HistoryEntity> getHistory(@QueryParam("subject") SubjectEntity subject);

        @QueryMethod(QUERY_WHERE_OLDER)
        List<HistoryEntity> getHistory(@QueryParam("ageLimit") Date ageLimit);

        @UpdateMethod(QUERY_DELETE_ALL)
        void deleteAll(@QueryParam("subject") SubjectEntity subject);

        @UpdateMethod(QUERY_DELETE_WHERE_OLDER)
        void deleteWhereOlder(@QueryParam("ageLimit") Date ageLimit);
    }
}
