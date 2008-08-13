/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.AccessAuditEntity.COUNT_WHERE_CONTEXTID;
import static net.link.safeonline.entity.audit.AccessAuditEntity.QUERY_DELETE_WHERE_CONTEXTID;
import static net.link.safeonline.entity.audit.AccessAuditEntity.QUERY_LIST_USER;
import static net.link.safeonline.entity.audit.AccessAuditEntity.QUERY_WHERE_AGELIMIT;
import static net.link.safeonline.entity.audit.AccessAuditEntity.QUERY_WHERE_CONTEXTID;
import static net.link.safeonline.entity.audit.AccessAuditEntity.QUERY_WHERE_USER;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
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


/**
 * Access Audit entity.
 *
 * @author fcorneli
 *
 */
@Entity
@Table(name = "access_audit")
@NamedQueries( {
        @NamedQuery(name = QUERY_DELETE_WHERE_CONTEXTID, query = "DELETE " + "FROM AccessAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = QUERY_WHERE_CONTEXTID, query = "SELECT record " + "FROM AccessAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId"),
        @NamedQuery(name = COUNT_WHERE_CONTEXTID, query = "SELECT COUNT(*) " + "FROM AccessAuditEntity AS record "
                + "WHERE record.auditContext.id = :contextId AND " + "record.operationState IN (2, 3)"),
        @NamedQuery(name = QUERY_LIST_USER, query = "SELECT DISTINCT record.principal "
                + "FROM AccessAuditEntity AS record " + "WHERE record.principal IS NOT NULL"),
        @NamedQuery(name = QUERY_WHERE_USER, query = "SELECT record " + "FROM AccessAuditEntity AS record "
                + "WHERE record.principal = :principal"),
        @NamedQuery(name = QUERY_WHERE_AGELIMIT, query = "SELECT record " + "FROM AccessAuditEntity AS record "
                + "WHERE record.eventDate > :ageLimit") })
public class AccessAuditEntity implements Serializable {

    private static final long  serialVersionUID             = 1L;

    public static final String QUERY_DELETE_WHERE_CONTEXTID = "aca.del.id";

    public static final String QUERY_WHERE_CONTEXTID        = "aca.id";

    public static final String QUERY_WHERE_AGELIMIT         = "aca.age";

    public static final String QUERY_WHERE_USER             = "aca.user";

    public static final String QUERY_LIST_USER              = "aca.list.user";

    public static final String COUNT_WHERE_CONTEXTID        = "aca.count.id";

    private Long               id;

    private AuditContextEntity auditContext;

    private String             operation;

    private OperationStateType operationState;

    private String             principal;

    private Date               eventDate;


    public AccessAuditEntity() {

        // empty
    }

    public AccessAuditEntity(AuditContextEntity auditContext, String operation, OperationStateType operationState,
            String principal) {

        this.auditContext = auditContext;
        this.operation = operation;
        this.operationState = operationState;
        this.principal = principal;
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

    @ManyToOne
    public AuditContextEntity getAuditContext() {

        return this.auditContext;
    }

    public void setAuditContext(AuditContextEntity auditContext) {

        this.auditContext = auditContext;
    }

    @Column(nullable = false)
    public String getOperation() {

        return this.operation;
    }

    public void setOperation(String operation) {

        this.operation = operation;
    }

    public String getPrincipal() {

        return this.principal;
    }

    public void setPrincipal(String principal) {

        this.principal = principal;
    }

    @Enumerated(EnumType.ORDINAL)
    public OperationStateType getOperationState() {

        return this.operationState;
    }

    public void setOperationState(OperationStateType operationState) {

        this.operationState = operationState;
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
        List<AccessAuditEntity> listRecords(@QueryParam("contextId") Long id);

        @QueryMethod(COUNT_WHERE_CONTEXTID)
        long countErrorRecords(@QueryParam("contextId") long id);

        @QueryMethod(QUERY_WHERE_AGELIMIT)
        List<AccessAuditEntity> listRecordsSince(@QueryParam("ageLimit") Date ageLimit);

        @QueryMethod(QUERY_LIST_USER)
        List<String> listUsers();

        @QueryMethod(QUERY_WHERE_USER)
        List<AccessAuditEntity> listUserRecords(@QueryParam("principal") String principal);
    }
}
