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
import org.apache.commons.lang.builder.ToStringStyle;


@Embeddable
public class ApplicationIdentityAttributePK implements Serializable {

    public static final long   INITIAL_IDENTITY_VERSION   = 1;

    private static final long  serialVersionUID           = 1L;

    public static final String APPLICATION_ID_COLUMN      = "applicationId";
    public static final String IDENTITY_VERSION_COLUMN    = "identityVersion";
    public static final String ATTRIBUTE_TYPE_NAME_COLUMN = "attributeTypeName";

    private long               applicationId;

    private long               identityVersion;

    private String             attributeTypeName;


    public ApplicationIdentityAttributePK() {

        // empty
    }

    public ApplicationIdentityAttributePK(long applicationId, long identityVersion, String attributeTypeName) {

        this.applicationId = applicationId;
        this.identityVersion = identityVersion;
        this.attributeTypeName = attributeTypeName;
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

    @Column(name = ATTRIBUTE_TYPE_NAME_COLUMN)
    public String getAttributeTypeName() {

        return attributeTypeName;
    }

    public void setAttributeTypeName(String attributeTypeName) {

        this.attributeTypeName = attributeTypeName;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof ApplicationIdentityAttributePK)
            return false;
        ApplicationIdentityAttributePK rhs = (ApplicationIdentityAttributePK) obj;
        return new EqualsBuilder().append(applicationId, rhs.applicationId).append(identityVersion, rhs.identityVersion).append(
                attributeTypeName, rhs.attributeTypeName).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(applicationId).append(identityVersion).append(attributeTypeName).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("applicationId", applicationId).append("identityVersion",
                identityVersion).append("attributeType", attributeTypeName).toString();
    }
}
