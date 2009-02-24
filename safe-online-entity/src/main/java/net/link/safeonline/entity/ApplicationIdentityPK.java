/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Embeddable
public class ApplicationIdentityPK implements Serializable {

    private static final long  serialVersionUID         = 1L;

    public static final long   INITIAL_IDENTITY_VERSION = 1;

    public static final String APPLICATION_ID_COLUMN    = "applicationId";
    public static final String IDENTITY_VERSION_COLUMN  = "identityVersion";

    private long               applicationId;

    private long               identityVersion;


    public ApplicationIdentityPK() {

        // empty
    }

    public ApplicationIdentityPK(long applicationId, long identityVersion) {

        this.applicationId = applicationId;
        this.identityVersion = identityVersion;
    }

    public ApplicationIdentityPK(ApplicationEntity application) {

        applicationId = application.getId();
        identityVersion = INITIAL_IDENTITY_VERSION;
    }

    @Column(name = APPLICATION_ID_COLUMN)
    public long getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(long applicationId) {

        this.applicationId = applicationId;
    }

    @Column(name = IDENTITY_VERSION_COLUMN)
    public long getIdentityVersion() {

        return identityVersion;
    }

    public void setIdentityVersion(long identityVersion) {

        this.identityVersion = identityVersion;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof ApplicationIdentityPK)
            return false;
        ApplicationIdentityPK rhs = (ApplicationIdentityPK) obj;
        return new EqualsBuilder().append(applicationId, rhs.applicationId).append(identityVersion, rhs.identityVersion).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(applicationId).append(identityVersion).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("applicationId", applicationId).append("identityVersion", identityVersion).toString();
    }
}
