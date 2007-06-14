/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.IndexColumn;

import static net.link.safeonline.entity.AttributeTypeEntity.QUERY_WHERE_ALL;
import static net.link.safeonline.entity.AttributeTypeEntity.QUERY_CATEGORIZE;

@Entity
@Table(name = "attribute_type")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_ALL, query = "FROM AttributeTypeEntity"),
		@NamedQuery(name = QUERY_CATEGORIZE, query = "SELECT a.stringValue, COUNT(a.stringValue) "
				+ "FROM AttributeEntity a, SubscriptionEntity s, "
				+ "ApplicationIdentityEntity i, ApplicationIdentityAttributeEntity aia "
				+ "WHERE a.subject = s.subject "
				+ "AND s.confirmedIdentityVersion = i.pk.identityVersion "
				+ "AND s.application = i.application "
				+ "AND :application = s.application "
				+ "AND aia.applicationIdentity = i "
				+ "AND :attributeType = aia.attributeType "
				+ "AND aia.attributeType = a.attributeType "
				+ "GROUP BY a.stringValue") })
public class AttributeTypeEntity implements Serializable {

	public static final String QUERY_WHERE_ALL = "at.all";

	public static final String QUERY_CATEGORIZE = "at.cat";

	private static final long serialVersionUID = 1L;

	private String name;

	private DatatypeType type;

	private boolean userVisible;

	private boolean userEditable;

	private boolean multivalued;

	private boolean compoundMember;

	private Map<String, AttributeTypeDescriptionEntity> descriptions;

	private List<CompoundedAttributeTypeMemberEntity> members;

	public AttributeTypeEntity() {
		this(null, null, false, false);
	}

	public AttributeTypeEntity(String name, DatatypeType type,
			boolean userVisible, boolean userEditable) {
		this.name = name;
		this.type = type;
		this.userVisible = userVisible;
		this.userEditable = userEditable;
		this.descriptions = new HashMap<String, AttributeTypeDescriptionEntity>();
		this.members = new LinkedList<CompoundedAttributeTypeMemberEntity>();
	}

	@Id
	@Column(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	public DatatypeType getType() {
		return this.type;
	}

	public void setType(DatatypeType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AttributeTypeEntity rhs = (AttributeTypeEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).toString();
	}

	public boolean isUserVisible() {
		return this.userVisible;
	}

	public void setUserVisible(boolean userVisible) {
		this.userVisible = userVisible;
	}

	public boolean isUserEditable() {
		return this.userEditable;
	}

	public void setUserEditable(boolean userEditable) {
		this.userEditable = userEditable;
	}

	/**
	 * Marks whether this attribute type allows for multivalued attributes.
	 * 
	 * @return
	 */
	public boolean isMultivalued() {
		return this.multivalued;
	}

	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}

	/**
	 * Marks whether this attribute type is a member of a compounded attribute
	 * type. This field is used to have a performant implementation of the
	 * restriction that an attribute type can only participate in one compounded
	 * attribute type.
	 * 
	 * @return
	 */
	public boolean isCompoundMember() {
		return this.compoundMember;
	}

	public void setCompoundMember(boolean compoundMember) {
		this.compoundMember = compoundMember;
	}

	@OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@IndexColumn(name = CompoundedAttributeTypeMemberEntity.MEMBER_SEQUENCE_COLUMN_NAME)
	public List<CompoundedAttributeTypeMemberEntity> getMembers() {
		return this.members;
	}

	public void setMembers(List<CompoundedAttributeTypeMemberEntity> members) {
		this.members = members;
	}

	public void addMember(AttributeTypeEntity memberAttributeType,
			int memberSequence, boolean required) {
		CompoundedAttributeTypeMemberEntity member = new CompoundedAttributeTypeMemberEntity(
				this, memberAttributeType, memberSequence, required);
		getMembers().add(member);
	}

	@Transient
	public boolean isCompounded() {
		return false == this.members.isEmpty();
	}

	@OneToMany(mappedBy = "attributeType")
	@MapKey(name = "language")
	public Map<String, AttributeTypeDescriptionEntity> getDescriptions() {
		return this.descriptions;
	}

	public void setDescriptions(
			Map<String, AttributeTypeDescriptionEntity> descriptions) {
		this.descriptions = descriptions;
	}

	public static Query createQueryAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_ALL);
		return query;
	}

	public static Query createQueryCategorize(EntityManager entityManager,
			ApplicationEntity application, AttributeTypeEntity attributeType) {
		Query query = entityManager.createNamedQuery(QUERY_CATEGORIZE);
		query.setParameter("attributeType", attributeType);
		query.setParameter("application", application);
		return query;
	}
}
