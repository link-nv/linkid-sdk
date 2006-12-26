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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "attribute")
public class AttributeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private AttributePK pk;

	private AttributeTypeEntity attributeType;

	private SubjectEntity subject;

	private String stringValue;

	public AttributeEntity() {
		// empty
	}

	public AttributeEntity(String attributeTypeName, String subjectLogin,
			String stringValue) {
		this.stringValue = stringValue;
		this.pk = new AttributePK(attributeTypeName, subjectLogin);
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
}
