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


@Embeddable
public class CompoundedAttributeTypeMemberPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            parent;

    private String            member;


    public CompoundedAttributeTypeMemberPK() {

        // empty
    }

    public CompoundedAttributeTypeMemberPK(AttributeTypeEntity parentAttributeType, AttributeTypeEntity memberAttributeType) {

        this.parent = parentAttributeType.getName();
        this.member = memberAttributeType.getName();
    }

    public String getMember() {

        return this.member;
    }

    public void setMember(String member) {

        this.member = member;
    }

    public String getParent() {

        return this.parent;
    }

    public void setParent(String parent) {

        this.parent = parent;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof CompoundedAttributeTypeMemberPK)
            return false;
        CompoundedAttributeTypeMemberPK rhs = (CompoundedAttributeTypeMemberPK) obj;
        return new EqualsBuilder().append(this.parent, rhs.parent).append(this.member, rhs.member).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.parent).append(this.member).toHashCode();
    }
}
