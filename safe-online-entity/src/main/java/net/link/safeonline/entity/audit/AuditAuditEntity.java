/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.AuditAuditEntity.COUNT_WHERE_CONTEXTID;
import static net.link.safeonline.entity.audit.AuditAuditEntity.QUERY_ALL;
import static net.link.safeonline.entity.audit.AuditAuditEntity.QUERY_DELETE_WHERE_CONTEXTID;
import static net.link.safeonline.entity.audit.AuditAuditEntity.QUERY_WHERE_AGELIMIT;
import static net.link.safeonline.entity.audit.AuditAuditEntity.QUERY_WHERE_CONTEXTID;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.hibernate.annotations.Index;


/**
 * Audit entity about audit system itself.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "audit_audit")
@NamedQueries( {
        @NamedQuery(name = QUERY_DELETE_WHERE_CONTEXTID, query = "DELETE " + "FROM AuditAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = QUERY_WHERE_CONTEXTID, query = "SELECT record " + "FROM AuditAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = COUNT_WHERE_CONTEXTID, query = "SELECT COUNT(*) " + "FROM AuditAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = QUERY_WHERE_AGELIMIT, query = "SELECT record " + "FROM AuditAuditEntity AS record "
                + "WHERE record.eventDate > :ageLimit"), @NamedQuery(name = QUERY_ALL, query = "FROM AuditAuditEntity") })
public class AuditAuditEntity implements Serializable {

    private static final long  serialVersionUID             = 1L;

    public static final String QUERY_DELETE_WHERE_CONTEXTID = "aa.del.id";

    public static final String QUERY_WHERE_CONTEXTID        = "aa.id";

    public static final String QUERY_WHERE_AGELIMIT         = "aa.age";

    public static final String COUNT_WHERE_CONTEXTID        = "aa.count.id";

    public static final String QUERY_ALL                    = "aa.all";

    private Long               id;

    private AuditContextEntity auditContext;

    private String             message;

    private Date               eventDate;


    public AuditAuditEntity() {

        // empty
    }

    public AuditAuditEntity(AuditContextEntity auditContext, String message) {

        this.auditContext = auditContext;
        this.message = message;
        eventDate = new Date();
    }

    public AuditAuditEntity(String message) {

        this(null, message);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    @ManyToOne
    @Index(name = "auditAuditIndex")
    public AuditContextEntity getAuditContext() {

        return auditContext;
    }

    public void setAuditContext(AuditContextEntity auditContext) {

        this.auditContext = auditContext;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getEventDate() {

        return eventDate;
    }

    public void setEventDate(Date eventDate) {

        this.eventDate = eventDate;
    }


    public interface QueryInterface {

        @UpdateMethod(QUERY_DELETE_WHERE_CONTEXTID)
        void deleteRecords(@QueryParam("contextId") Long contextId);

        @QueryMethod(QUERY_WHERE_CONTEXTID)
        List<AuditAuditEntity> listRecords(@QueryParam("contextId") Long id);

        @QueryMethod(COUNT_WHERE_CONTEXTID)
        long countRecords(@QueryParam("contextId") long id);

        @QueryMethod(QUERY_WHERE_AGELIMIT)
        List<AuditAuditEntity> listRecordsSince(@QueryParam("ageLimit") Date ageLimit);

        @QueryMethod(QUERY_ALL)
        List<AuditAuditEntity> listRecords();
    }
}
