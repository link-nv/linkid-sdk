/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

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

import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT;
import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT_AND_VISIBLE;
import static net.link.safeonline.entity.AttributeEntity.SUBJECT_PARAM;

@Entity
@Table(name = "attribute")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT attribute FROM AttributeEntity AS attribute "
				+ "WHERE attribute.subject = :" + SUBJECT_PARAM),
		@NamedQuery(name = QUERY_WHERE_SUBJECT_AND_VISIBLE, query = "SELECT attribute FROM AttributeEntity AS attribute "
				+ "WHERE attribute.subject = :"
				+ SUBJECT_PARAM
				+ " AND attribute.attributeType.userVisible = TRUE") })
public class AttributeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_SUBJECT = "attr.subject";

	public static final String QUERY_WHERE_SUBJECT_AND_VISIBLE = "attr.subject.visi";

	public static final String SUBJECT_PARAM = "subject";

	private AttributePK pk;

	private AttributeTypeEntity attributeType;

	private SubjectEntity subject;

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

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "attributeType", column = @Column(name = "attributeType")),
			@AttributeOverride(name = "subject", column = @Column(name = "subject")) })
	public AttributePK getPk() {
		return this.pk;
	}

	public void setPk(AttributePK pk) {
		this.pk = pk;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "attributeType", insertable = false, updatable = false)
	public AttributeTypeEntity getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(AttributeTypeEntity attributeType) {
		this.attributeType = attributeType;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "subject", insertable = false, updatable = false)
	public SubjectEntity getSubject() {
		return this.subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
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
}
