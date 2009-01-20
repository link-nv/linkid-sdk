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


/**
 * Primary key class for {@link AttributeEntity}.
 * 
 * @author fcorneli
 * 
 */
@Embeddable
public class AttributePK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            attributeType;

    private String            subject;

    private long              attributeIndex;


    public AttributePK() {

        // empty
    }

    public AttributePK(String attributeType, String subject) {

        this(attributeType, subject, 0);
    }

    public AttributePK(String attributeType, String subject, long attributeIndex) {

        this.attributeType = attributeType;
        this.subject = subject;
        this.attributeIndex = attributeIndex;
    }

    public AttributePK(AttributeTypeEntity attributeType, SubjectEntity subject) {

        this(attributeType, subject, 0);
    }

    public AttributePK(AttributeTypeEntity attributeType, SubjectEntity subject, long attributeIndex) {

        this(attributeType.getName(), subject.getUserId(), attributeIndex);
    }

    public String getAttributeType() {

        return attributeType;
    }

    public void setAttributeType(String attributeType) {

        this.attributeType = attributeType;
    }

    public String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    /**
     * The attribute index is used for implementing the multi-valued attributes. For single-value attributes that attribute index is zero.
     * 
     */
    public long getAttributeIndex() {

        return attributeIndex;
    }

    public void setAttributeIndex(long attributeIndex) {

        this.attributeIndex = attributeIndex;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof AttributePK)
            return false;
        AttributePK rhs = (AttributePK) obj;
        return new EqualsBuilder().append(subject, rhs.subject).append(attributeType, rhs.attributeType).append(
                attributeIndex, rhs.attributeIndex).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(subject).append(attributeType).append(attributeIndex).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("subject", subject).append("attributeType", attributeType).append("index",
                attributeIndex).toString();
    }
}
