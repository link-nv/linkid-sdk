/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.SecurityAuditEntity.QUERY_WHERE_AGELIMIT;
import static net.link.safeonline.entity.audit.SecurityAuditEntity.QUERY_WHERE_CONTEXTID;
import static net.link.safeonline.entity.audit.SecurityAuditEntity.QUERY_ALL;
import static net.link.safeonline.entity.audit.SecurityAuditEntity.QUERY_DELETE_WHERE_CONTEXTID;
import static net.link.safeonline.entity.audit.SecurityAuditEntity.QUERY_LIST_USER;
import static net.link.safeonline.entity.audit.SecurityAuditEntity.QUERY_WHERE_USER;
import static net.link.safeonline.entity.audit.SecurityAuditEntity.COUNT_WHERE_CONTEXTID;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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


@Entity
@Table(name = "security_audit")
@NamedQueries( {
        @NamedQuery(name = QUERY_DELETE_WHERE_CONTEXTID, query = "DELETE " + "FROM SecurityAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = QUERY_ALL, query = "FROM SecurityAuditEntity"),
        @NamedQuery(name = QUERY_WHERE_CONTEXTID, query = "SELECT record " + "FROM SecurityAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = COUNT_WHERE_CONTEXTID, query = "SELECT COUNT(*) " + "FROM SecurityAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = QUERY_LIST_USER, query = "SELECT DISTINCT record.targetPrincipal "
                + "FROM SecurityAuditEntity AS record " + "WHERE record.targetPrincipal IS NOT NULL"),
        @NamedQuery(name = QUERY_WHERE_USER, query = "SELECT record " + "FROM SecurityAuditEntity AS record "
                + "WHERE record.targetPrincipal = :principal"),
        @NamedQuery(name = QUERY_WHERE_AGELIMIT, query = "SELECT record " + "FROM SecurityAuditEntity AS record "
                + "WHERE record.eventDate > :ageLimit") })
public class SecurityAuditEntity implements Serializable {

    private static final long  serialVersionUID             = 1L;

    public static final String QUERY_DELETE_WHERE_CONTEXTID = "sa.del.id";

    public static final String QUERY_ALL                    = "sa.all";

    public static final String QUERY_WHERE_CONTEXTID        = "sa.id";

    public static final String QUERY_WHERE_AGELIMIT         = "sa.age";

    public static final String QUERY_LIST_USER              = "sa.list.user";

    public static final String QUERY_WHERE_USER             = "sa.user";

    public static final String COUNT_WHERE_CONTEXTID        = "sa.count.id";

    private Long               id;

    private AuditContextEntity auditContext;

    private String             message;

    private SecurityThreatType securityThreat;

    private String             targetPrincipal;

    private Date               eventDate;


    public SecurityAuditEntity() {

        // empty
    }

    public SecurityAuditEntity(AuditContextEntity auditContext, SecurityThreatType securityThreat,
            String targetPrincipal, String message) {

        this.auditContext = auditContext;
        this.securityThreat = securityThreat;
        this.message = message;
        this.targetPrincipal = targetPrincipal;
        this.eventDate = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {

        return this.id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getMessage() {

        return this.message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    @Enumerated(EnumType.STRING)
    public SecurityThreatType getSecurityThreat() {

        return this.securityThreat;
    }

    public void setSecurityThreat(SecurityThreatType securityThreat) {

        this.securityThreat = securityThreat;
    }

    @ManyToOne
    public AuditContextEntity getAuditContext() {

        return this.auditContext;
    }

    public void setAuditContext(AuditContextEntity auditContext) {

        this.auditContext = auditContext;
    }

    public String getTargetPrincipal() {

        return this.targetPrincipal;
    }

    public void setTargetPrincipal(String targetPrincipal) {

        this.targetPrincipal = targetPrincipal;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getEventDate() {

        return this.eventDate;
    }

    public void setEventDate(Date eventDate) {

        this.eventDate = eventDate;
    }


    public interface QueryInterface {

        @UpdateMethod(QUERY_DELETE_WHERE_CONTEXTID)
        void deleteRecords(@QueryParam("contextId") Long contextId);

        @QueryMethod(QUERY_WHERE_CONTEXTID)
        List<SecurityAuditEntity> listRecords(@QueryParam("contextId") Long id);

        @QueryMethod(COUNT_WHERE_CONTEXTID)
        long countRecords(@QueryParam("contextId") long id);

        @QueryMethod(QUERY_ALL)
        List<SecurityAuditEntity> listRecords();

        @QueryMethod(QUERY_WHERE_AGELIMIT)
        List<SecurityAuditEntity> listRecordsSince(@QueryParam("ageLimit") Date ageLimit);

        @QueryMethod(QUERY_LIST_USER)
        List<String> listUsers();

        @QueryMethod(QUERY_WHERE_USER)
        List<SecurityAuditEntity> listUserRecords(@QueryParam("principal") String principal);
    }
}
