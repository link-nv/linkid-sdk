/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity.sessiontracking;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
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
import javax.persistence.UniqueConstraint;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * <h2>{@link SessionTrackingEntity}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 31, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */

@Entity
@Table(name = "session_tracker", uniqueConstraints = @UniqueConstraint(columnNames = { SessionTrackingEntity.APPLICATION_COLUMN_NAME,
        SessionTrackingEntity.SESSION_COLUMN_NAME, SessionTrackingEntity.SSO_ID_COLUMN_NAME,
        SessionTrackingEntity.APPLICATION_POOL_COLUMN_NAME }))
@NamedQueries( { @NamedQuery(name = SessionTrackingEntity.QUERY_WHERE, query = "SELECT tracker " + "FROM SessionTrackingEntity AS tracker "
        + "WHERE tracker.application = :application AND tracker.session = :session AND tracker.ssoId = :ssoId "
        + "AND tracker.applicationPool = :applicationPool") })
public class SessionTrackingEntity implements Serializable {

    private static final long     serialVersionUID             = 1L;

    public static final String    QUERY_WHERE                  = "tracker.where";

    public static final String    APPLICATION_COLUMN_NAME      = "application";
    public static final String    SESSION_COLUMN_NAME          = "session";
    public static final String    SSO_ID_COLUMN_NAME           = "ssoId";
    public static final String    APPLICATION_POOL_COLUMN_NAME = "applicationPool";

    private long                  id;

    private ApplicationEntity     application;

    private String                session;

    private String                ssoId;

    private ApplicationPoolEntity applicationPool;

    private Date                  timestamp;


    public SessionTrackingEntity() {

        // empty
    }

    public SessionTrackingEntity(ApplicationEntity application, String session, String ssoId, ApplicationPoolEntity applicationPool) {

        this.application = application;
        this.session = session;
        this.ssoId = ssoId;
        this.applicationPool = applicationPool;
        timestamp = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = APPLICATION_COLUMN_NAME)
    public ApplicationEntity getApplication() {

        return application;
    }

    public void setApplication(ApplicationEntity application) {

        this.application = application;
    }

    @Column(name = SESSION_COLUMN_NAME)
    public String getSession() {

        return session;
    }

    public void setSession(String session) {

        this.session = session;
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

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(Date timestamp) {

        this.timestamp = timestamp;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", id).append("timestamp", timestamp.toString()).toString();
    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_WHERE, nullable = true)
        SessionTrackingEntity find(@QueryParam("application") ApplicationEntity application, @QueryParam("session") String session,
                                   @QueryParam("ssoId") String ssoId, @QueryParam("applicationPool") ApplicationPoolEntity applicationPool);
    }
}
