/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Embeddable
public class ApplicationIdentityPK implements Serializable {

    public static final long  INITIAL_IDENTITY_VERSION = 1;

    private static final long serialVersionUID         = 1L;

    private String            application;

    private long              identityVersion;


    public ApplicationIdentityPK() {

        // empty
    }

    public ApplicationIdentityPK(String application, long identityVersion) {

        this.application = application;
        this.identityVersion = identityVersion;
    }

    public ApplicationIdentityPK(ApplicationEntity application) {

        this.application = application.getName();
        this.identityVersion = INITIAL_IDENTITY_VERSION;
    }

    public String getApplication() {

        return this.application;
    }

    public void setApplication(String application) {

        this.application = application;
    }

    public long getIdentityVersion() {

        return this.identityVersion;
    }

    public void setIdentityVersion(long identityVersion) {

        this.identityVersion = identityVersion;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (false == obj instanceof ApplicationIdentityPK) {
            return false;
        }
        ApplicationIdentityPK rhs = (ApplicationIdentityPK) obj;
        return new EqualsBuilder().append(this.application, rhs.application).append(this.identityVersion,
                rhs.identityVersion).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.application).append(this.identityVersion).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("application", this.application).append("identityVersion",
                this.identityVersion).toString();
    }
}
