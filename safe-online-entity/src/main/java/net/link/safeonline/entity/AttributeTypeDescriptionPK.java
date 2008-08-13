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
public class AttributeTypeDescriptionPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            attributeType;

    private String            language;


    public AttributeTypeDescriptionPK() {

        // empty
    }

    public AttributeTypeDescriptionPK(String attributeType, String language) {

        this.attributeType = attributeType;
        this.language = language;
    }

    public String getAttributeType() {

        return this.attributeType;
    }

    public void setAttributeType(String attributeType) {

        this.attributeType = attributeType;
    }

    public String getLanguage() {

        return this.language;
    }

    public void setLanguage(String language) {

        this.language = language;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (false == obj instanceof SubscriptionPK) {
            return false;
        }
        AttributeTypeDescriptionPK rhs = (AttributeTypeDescriptionPK) obj;
        return new EqualsBuilder().append(this.language, rhs.language).append(this.attributeType, rhs.attributeType)
                .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.language).append(this.attributeType).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("language", this.language).append("attributeType", this.attributeType)
                .toString();
    }
}
