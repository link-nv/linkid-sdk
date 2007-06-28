/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT;
import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT_AND_VISIBLE;
import static net.link.safeonline.entity.AttributeEntity.SUBJECT_PARAM;
import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE;
import static net.link.safeonline.entity.AttributeEntity.ATTRIBUTE_TYPE_PARAM;
import static net.link.safeonline.entity.AttributeEntity.MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE;

/**
 * Attribute JPA Entity. Sits as many-to-many between
 * {@link AttributeTypeEntity} and {@link SubjectEntity}. Multi-valued
 * attributes are implemented via the {@link #attributeIndex} field.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "attribute")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT attribute FROM AttributeEntity AS attribute "
				+ "WHERE attribute.subject = :" + SUBJECT_PARAM),
		@NamedQuery(name = QUERY_WHERE_SUBJECT_AND_VISIBLE, query = "SELECT attribute FROM AttributeEntity AS attribute "
				+ "WHERE attribute.subject = :"
				+ SUBJECT_PARAM
				+ " AND attribute.attributeType.userVisible = TRUE "
				+ "ORDER BY attribute.attributeType, attribute.attributeIndex"),
		@NamedQuery(name = QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE, query = "SELECT attribute FROM AttributeEntity AS attribute "
				+ "WHERE attribute.subject = :"
				+ SUBJECT_PARAM
				+ " AND attribute.attributeType = :"
				+ ATTRIBUTE_TYPE_PARAM
				+ " ORDER BY attribute.attributeIndex"),
		@NamedQuery(name = MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE, query = "SELECT MAX(attribute.attributeIndex) FROM AttributeEntity AS attribute "
				+ "WHERE attribute.subject = :"
				+ SUBJECT_PARAM
				+ " AND attribute.attributeType = :" + ATTRIBUTE_TYPE_PARAM) })
public class AttributeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_SUBJECT = "attr.subject";

	public static final String QUERY_WHERE_SUBJECT_AND_VISIBLE = "attr.subject.visi";

	public static final String QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE = "attr.subject.at";

	public static final String MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE = "max.id.subject.at";

	public static final String SUBJECT_PARAM = "subject";

	public static final String ATTRIBUTE_TYPE_PARAM = "attributeType";

	private AttributePK pk;

	private AttributeTypeEntity attributeType;

	private SubjectEntity subject;

	private long attributeIndex;

	private String stringValue;

	private Boolean booleanValue;

	public AttributeEntity() {
		// empty
	}

	public AttributeEntity(AttributeTypeEntity attributeType,
			SubjectEntity subject, String stringValue) {
		this.stringValue = stringValue;
		this.attributeType = attributeType;
		this.subject = subject;
		this.pk = new AttributePK(attributeType.getName(), subject.getLogin());
	}

	public AttributeEntity(AttributeTypeEntity attributeType,
			SubjectEntity subject, long attributeIdx) {
		this.attributeType = attributeType;
		this.subject = subject;
		this.attributeIndex = attributeIdx;
		this.pk = new AttributePK(attributeType, subject, attributeIdx);
	}

	public static final String ATTRIBUTE_INDEX_COLUMN_NAME = "attribute_index";

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "attributeType", column = @Column(name = ATTRIBUTE_TYPE_COLUMN_NAME)),
			@AttributeOverride(name = "subject", column = @Column(name = SUBJECT_COLUMN_NAME)),
			@AttributeOverride(name = "attributeIndex", column = @Column(name = ATTRIBUTE_INDEX_COLUMN_NAME)) })
	public AttributePK getPk() {
		return this.pk;
	}

	public void setPk(AttributePK pk) {
		this.pk = pk;
	}

	public static final String ATTRIBUTE_TYPE_COLUMN_NAME = "attribute_type";

	@ManyToOne(optional = false)
	@JoinColumn(name = ATTRIBUTE_TYPE_COLUMN_NAME, insertable = false, updatable = false)
	public AttributeTypeEntity getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(AttributeTypeEntity attributeType) {
		this.attributeType = attributeType;
	}

	public static final String SUBJECT_COLUMN_NAME = "subject";

	@ManyToOne(optional = false)
	@JoinColumn(name = SUBJECT_COLUMN_NAME, insertable = false, updatable = false)
	public SubjectEntity getSubject() {
		return this.subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
	}

	/**
	 * The attribute index is used for implementing the multi-valued attributes.
	 * For single-value attributes that attribute index is zero.
	 * 
	 * @return
	 */
	@Column(name = ATTRIBUTE_INDEX_COLUMN_NAME, insertable = false, updatable = false)
	public long getAttributeIndex() {
		return this.attributeIndex;
	}

	public void setAttributeIndex(long attributeIndex) {
		this.attributeIndex = attributeIndex;
	}

	public String getStringValue() {
		return this.stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Boolean getBooleanValue() {
		return this.booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	private transient List<AttributeEntity> members;

	/**
	 * We don't manage the member attributes of a compounded attribute directly
	 * via the database because the relationship is to complex to express. This
	 * field is filled in by the DAO layer upon request.
	 * 
	 * @return
	 */
	@Transient
	public List<AttributeEntity> getMembers() {
		if (null == this.members) {
			this.members = new LinkedList<AttributeEntity>();
		}
		return this.members;
	}

	public void setMembers(List<AttributeEntity> members) {
		this.members = members;
	}

	/**
	 * Generic data mapping can be done via {@link #getValue()} and
	 * {@link #setValue(Object)}.
	 * 
	 * @return
	 */
	@Transient
	public Object getValue() {
		DatatypeType datatype = this.attributeType.getType();
		switch (datatype) {
		case STRING:
			return this.getStringValue();
		case BOOLEAN:
			return this.getBooleanValue();
		default:
			throw new EJBException("datatype not supported: " + datatype);
		}
	}

	@Transient
	public void setValue(Object value) {
		DatatypeType datatype = this.attributeType.getType();
		switch (datatype) {
		case STRING: {
			String stringValue = (String) value;
			this.setStringValue(stringValue);
			break;
		}
		case BOOLEAN: {
			Boolean booleanValue = (Boolean) value;
			this.setBooleanValue(booleanValue);
			break;
		}
		default:
			throw new EJBException("datatype not supported: " + datatype);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == (obj instanceof AttributeEntity)) {
			return false;
		}
		AttributeEntity rhs = (AttributeEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pk).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("pk", this.pk).toString();
	}

	public static Query createQueryWhereSubject(EntityManager entityManager,
			SubjectEntity subject) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_SUBJECT);
		query.setParameter(SUBJECT_PARAM, subject);
		return query;
	}

	public static Query createQueryWhereSubjectAndVisible(
			EntityManager entityManager, SubjectEntity subject) {
		Query query = entityManager
				.createNamedQuery(QUERY_WHERE_SUBJECT_AND_VISIBLE);
		query.setParameter(SUBJECT_PARAM, subject);
		return query;
	}

	public static Query createQueryWhereSubjectAndAttributeTypeOrdered(
			EntityManager entityManager, SubjectEntity subject,
			AttributeTypeEntity attributeType) {
		Query query = entityManager
				.createNamedQuery(QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE);
		query.setParameter(SUBJECT_PARAM, subject);
		query.setParameter(ATTRIBUTE_TYPE_PARAM, attributeType);
		return query;
	}

	public static Query createMaxIdWhereSubjectAndAttributeType(
			EntityManager entityManager, SubjectEntity subject,
			AttributeTypeEntity attributeType) {
		Query query = entityManager
				.createNamedQuery(MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE);
		query.setParameter(SUBJECT_PARAM, subject);
		query.setParameter(ATTRIBUTE_TYPE_PARAM, attributeType);
		return query;
	}
}
