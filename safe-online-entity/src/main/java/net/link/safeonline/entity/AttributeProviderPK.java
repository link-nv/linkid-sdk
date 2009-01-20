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
public class AttributeProviderPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            applicationName;

    private String            attributeTypeName;


    public AttributeProviderPK() {

        // empty
    }

    public AttributeProviderPK(String applicationName, String attributeTypeName) {

        this.applicationName = applicationName;
        this.attributeTypeName = attributeTypeName;
    }

    public AttributeProviderPK(ApplicationEntity application, AttributeTypeEntity attributeType) {

        applicationName = application.getName();
        attributeTypeName = attributeType.getName();
    }

    public String getApplicationName() {

        return applicationName;
    }

    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

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
        return new EqualsBuilder().append(applicationName, rhs.applicationName).append(attributeTypeName, rhs.attributeTypeName)
                                  .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(applicationName).append(attributeTypeName).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("application", applicationName).append("attribute type", attributeTypeName)
                                        .toString();
    }
}
