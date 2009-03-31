/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity.sessiontracking;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * <h2>{@link SessionTrackingPK}<br>
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

@Embeddable
public class SessionTrackingPK implements Serializable {

    private static final long  serialVersionUID             = 1L;

    public static final String APPLICATION_ID_COLUMN        = "applicationId";
    public static final String SESSION_COLUMN               = "session";
    public static final String SSO_ID_COLUMN                = "ssoId";
    public static final String APPLICATION_POOL_NAME_COLUMN = "applicationPoolName";

    private long               applicationId;

    private String             session;

    private String             ssoId;

    private String             applicationPoolName;


    public SessionTrackingPK() {

        // empty
    }

    public SessionTrackingPK(long applicationId, String session, String ssoId, String applicationPoolName) {

        this.applicationId = applicationId;
        this.session = session;
        this.ssoId = ssoId;
        this.applicationPoolName = applicationPoolName;

    }

    @Column(name = APPLICATION_ID_COLUMN)
    public long getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(long applicationId) {

        this.applicationId = applicationId;
    }

    @Column(name = SESSION_COLUMN)
    public String getSession() {

        return session;

    }

    public void setSession(String session) {

        this.session = session;
    }

    @Column(name = SSO_ID_COLUMN)
    public String getSsoId() {

        return ssoId;
    }

    public void setSsoId(String ssoId) {

        this.ssoId = ssoId;
    }

    @Column(name = APPLICATION_POOL_NAME_COLUMN)
    public String getApplicationPoolName() {

        return applicationPoolName;
    }

    public void setApplicationPoolName(String applicationPoolName) {

        this.applicationPoolName = applicationPoolName;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof SessionTrackingPK)
            return false;
        SessionTrackingPK rhs = (SessionTrackingPK) obj;
        return new EqualsBuilder().append(applicationId, rhs.applicationId).append(session, rhs.session).append(ssoId, rhs.ssoId).append(
                applicationPoolName, rhs.applicationPoolName).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(applicationId).append(session).append(ssoId).append(applicationPoolName).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("applicationId", applicationId).append("session", session).append("ssoId", ssoId).append(
                "applicationPoolName", applicationPoolName).toString();
    }

}
