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
public class AttributeProviderPK implements Serializable {

    private static final long  serialVersionUID           = 1L;

    public static final String APPLICATION_ID_COLUMN      = "applicationId";
    public static final String ATTRIBUTE_TYPE_NAME_COLUMN = "attributeTypeName";

    private long               applicationId;

    private String             attributeTypeName;


    public AttributeProviderPK() {

        // empty
    }

    public AttributeProviderPK(long applicationId, String attributeTypeName) {

        this.applicationId = applicationId;
        this.attributeTypeName = attributeTypeName;
    }

    public AttributeProviderPK(ApplicationEntity application, AttributeTypeEntity attributeType) {

        applicationId = application.getId();
        attributeTypeName = attributeType.getName();
    }

    @Column(name = APPLICATION_ID_COLUMN)
    public long getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(long applicationId) {

        this.applicationId = applicationId;
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
        if (null == obj)
            return false;
        if (false == obj instanceof AttributeProviderPK)
            return false;
        AttributeProviderPK rhs = (AttributeProviderPK) obj;
        return new EqualsBuilder().append(applicationId, rhs.applicationId).append(attributeTypeName, rhs.attributeTypeName).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(applicationId).append(attributeTypeName).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("applicationId", applicationId).append("attribute type", attributeTypeName).toString();
    }
}
