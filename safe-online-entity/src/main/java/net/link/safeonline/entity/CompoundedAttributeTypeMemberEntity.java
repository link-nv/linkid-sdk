/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "comp_attribute_member")
public class CompoundedAttributeTypeMemberEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private CompoundedAttributeTypeMemberPK pk;

	private AttributeTypeEntity parent;

	private AttributeTypeEntity member;

	private int memberSequence;

	public CompoundedAttributeTypeMemberEntity() {
		// empty
	}

	public CompoundedAttributeTypeMemberEntity(AttributeTypeEntity parent,
			AttributeTypeEntity member, int memberSequence) {
		this.parent = parent;
		this.member = member;
		this.memberSequence = memberSequence;
		this.pk = new CompoundedAttributeTypeMemberPK(this.parent, this.member);
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "parent", column = @Column(name = PARENT_COLUMN_NAME)),
			@AttributeOverride(name = "member", column = @Column(name = MEMBER_COLUMN_NAME)) })
	public CompoundedAttributeTypeMemberPK getPk() {
		return this.pk;
	}

	public void setPk(CompoundedAttributeTypeMemberPK pk) {
		this.pk = pk;
	}

	public static final String PARENT_COLUMN_NAME = "parent_attribute_type";

	@ManyToOne(optional = false)
	@JoinColumn(name = PARENT_COLUMN_NAME, insertable = false, updatable = false)
	public AttributeTypeEntity getParent() {
		return this.parent;
	}

	public void setParent(AttributeTypeEntity parent) {
		this.parent = parent;
	}

	public static final String MEMBER_COLUMN_NAME = "member_attribute_type";

	@ManyToOne(optional = false)
	@JoinColumn(name = MEMBER_COLUMN_NAME, insertable = false, updatable = false)
	public AttributeTypeEntity getMember() {
		return this.member;
	}

	public void setMember(AttributeTypeEntity member) {
		this.member = member;
	}

	public static final String MEMBER_SEQUENCE_COLUMN_NAME = "memberSequence";

	@Column(name = MEMBER_SEQUENCE_COLUMN_NAME)
	public int getMemberSequence() {
		return this.memberSequence;
	}

	public void setMemberSequence(int memberSequence) {
		this.memberSequence = memberSequence;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof CompoundedAttributeTypeMemberEntity) {
			return false;
		}
		CompoundedAttributeTypeMemberEntity rhs = (CompoundedAttributeTypeMemberEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pk).toHashCode();
	}
}
