/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity.sessiontracking;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * <h2>{@link SessionAssertionEntity}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 1, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Entity
@Table(name = "session_assertion", uniqueConstraints = @UniqueConstraint(columnNames = { SessionAssertionEntity.SSO_ID_COLUMN_NAME,
        SessionAssertionEntity.APPLICATION_POOL_COLUMN_NAME }))
@NamedQueries( {
        @NamedQuery(name = SessionAssertionEntity.QUERY_WHERE, query = "SELECT assertion FROM SessionAssertionEntity AS assertion "
                + "WHERE assertion.ssoId = :ssoId AND assertion.applicationPool = :applicationPool"),
        @NamedQuery(name = SessionAssertionEntity.QUERY_WHERE_SUBJECT, query = "SELECT assertion FROM SessionAssertionEntity AS assertion "
                + "WHERE assertion.subject = :subject") })
public class SessionAssertionEntity implements Serializable {

    private static final long                 serialVersionUID             = 1L;

    public static final String                QUERY_WHERE                  = "assertion.where";
    public static final String                QUERY_WHERE_SUBJECT          = "assertion.where.subject";

    public static final String                SSO_ID_COLUMN_NAME           = "ssoId";
    public static final String                APPLICATION_POOL_COLUMN_NAME = "applicationPool";

    private long                              id;

    private String                            ssoId;

    private ApplicationPoolEntity             applicationPool;

    private SubjectEntity                     subject;

    private List<SessionAuthnStatementEntity> statements;


    public SessionAssertionEntity() {

        // empty
    }

    public SessionAssertionEntity(String ssoId, ApplicationPoolEntity applicationPool) {

        this.ssoId = ssoId;
        this.applicationPool = applicationPool;
        statements = new LinkedList<SessionAuthnStatementEntity>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    @Column(name = SSO_ID_COLUMN_NAME)
    public String getSsoId() {

        return ssoId;
    }

    public void setSsoId(String ssoId) {

        this.ssoId = ssoId;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = APPLICATION_POOL_COLUMN_NAME)
    public ApplicationPoolEntity getApplicationPool() {

        return applicationPool;
    }

    public void setApplicationPool(ApplicationPoolEntity applicationPool) {

        this.applicationPool = applicationPool;
    }

    @ManyToOne
    public SubjectEntity getSubject() {

        return subject;
    }

    public void setSubject(SubjectEntity subject) {

        this.subject = subject;
    }

    @OneToMany(mappedBy = SessionAuthnStatementEntity.SESSION_ASSERTION_COLUMN_NAME)
    public List<SessionAuthnStatementEntity> getStatements() {

        return statements;
    }

    public void setStatements(List<SessionAuthnStatementEntity> statements) {

        this.statements = statements;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof SessionAssertionEntity)
            return false;

        SessionAssertionEntity rhs = (SessionAssertionEntity) obj;
        return id == rhs.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return (int) id;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", id).append("applicationPool", applicationPool.getName())
                                                                    .toString();
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_WHERE, nullable = true)
        SessionAssertionEntity find(@QueryParam("ssoId") String ssoId, @QueryParam("applicationPool") ApplicationPoolEntity applicationPool);

        @QueryMethod(value = QUERY_WHERE_SUBJECT)
        List<SessionAssertionEntity> listAssertions(@QueryParam("subject") SubjectEntity subject);
    }
}
