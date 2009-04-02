/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity.sessiontracking;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;


/**
 * <h2>{@link SessionAuthnStatementEntity}<br>
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
@Table(name = "session_authn_statement")
@NamedQueries( { @NamedQuery(name = SessionAuthnStatementEntity.QUERY_WHERE_ASSERTION, query = "SELECT statement "
        + "FROM SessionAuthnStatementEntity AS statement " + "WHERE statement.assertion = :assertion") })
public class SessionAuthnStatementEntity implements Serializable {

    private static final long      serialVersionUID              = 1L;

    public static final String     QUERY_WHERE_ASSERTION         = "stat.q.ass";

    public static final String     DEVICE_COLUMN_NAME            = "device";
    public static final String     SESSION_ASSERTION_COLUMN_NAME = "assertion";

    private long                   id;

    private SessionAssertionEntity assertion;

    private Date                   authenticationTime;

    private DeviceEntity           device;


    public SessionAuthnStatementEntity() {

        // empty
    }

    public SessionAuthnStatementEntity(SessionAssertionEntity assertion, Date authenticationTime, DeviceEntity device) {

        this.assertion = assertion;
        this.authenticationTime = authenticationTime;
        this.device = device;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = SESSION_ASSERTION_COLUMN_NAME)
    public SessionAssertionEntity getAssertion() {

        return assertion;
    }

    public void setAssertion(SessionAssertionEntity assertion) {

        this.assertion = assertion;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getAuthenticationTime() {

        return authenticationTime;

    }

    public void setAuthenticationTime(Date authenticationTime) {

        this.authenticationTime = authenticationTime;
    }

    public DeviceEntity getDevice() {

        return device;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = DEVICE_COLUMN_NAME, insertable = false, updatable = false)
    public void setDevice(DeviceEntity device) {

        this.device = device;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof SessionAuthnStatementEntity)
            return false;

        SessionAuthnStatementEntity rhs = (SessionAuthnStatementEntity) obj;
        return id == rhs.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return (int) id;
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_WHERE_ASSERTION)
        List<SessionAuthnStatementEntity> listStatements(@QueryParam("assertion") SessionAssertionEntity assertion);
    }
}
