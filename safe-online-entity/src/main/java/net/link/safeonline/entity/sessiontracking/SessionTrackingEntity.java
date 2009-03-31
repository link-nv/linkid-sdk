/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity.sessiontracking;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;

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
@Table(name = "session_tracker")
public class SessionTrackingEntity implements Serializable {

    private static final long     serialVersionUID             = 1L;

    private static final String   APPLICATION_COLUMN_NAME      = "application";
    private static final String   APPLICATION_POOL_COLUMN_NAME = "applicationPool";

    private SessionTrackingPK      pk;

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

        pk = new SessionTrackingPK(application.getId(), session, ssoId, applicationPool.getName());
    }

    @EmbeddedId
    @AttributeOverrides( {
            @AttributeOverride(name = SessionTrackingPK.APPLICATION_ID_COLUMN, column = @Column(name = APPLICATION_COLUMN_NAME)),
            @AttributeOverride(name = SessionTrackingPK.APPLICATION_POOL_NAME_COLUMN, column = @Column(name = APPLICATION_POOL_COLUMN_NAME)) })
    public SessionTrackingPK getPk() {

        return pk;
    }

    public void setPk(SessionTrackingPK pk) {

        this.pk = pk;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = APPLICATION_COLUMN_NAME, insertable = false, updatable = false)
    public ApplicationEntity getApplication() {

        return application;
    }

    public void setApplication(ApplicationEntity application) {

        this.application = application;
    }

    public String getSession() {

        return session;
    }

    public void setSession(String session) {

        this.session = session;
    }

    public String getSsoId() {

        return ssoId;
    }

    public void setSsoId(String ssoId) {

        this.ssoId = ssoId;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = APPLICATION_POOL_COLUMN_NAME, insertable = false, updatable = false)
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
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof SessionTrackingEntity)
            return false;

        SessionTrackingEntity rhs = (SessionTrackingEntity) obj;
        return pk.equals(rhs.pk);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return pk.hashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("pk", pk).append("timestamp", timestamp.toString()).toString();
    }


    public interface QueryInterface {
    }
}
