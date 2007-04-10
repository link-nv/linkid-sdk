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

import static net.link.safeonline.entity.AttributeProviderEntity.QUERY_WHERE_ATTRIBUTE_TYPE;

/**
 * Definition of the attribute provider entity. This entity manages the write
 * and unconfirmed read access control of applications towards attributes.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "attribute_provider")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_ATTRIBUTE_TYPE, query = "SELECT attributeProvider FROM AttributeProviderEntity AS attributeProvider "
		+ "WHERE attributeProvider.attributeType = :attributeType") })
public class AttributeProviderEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_ATTRIBUTE_TYPE = "ape.at";

	private AttributeProviderPK pk;

	private ApplicationEntity application;

	private AttributeTypeEntity attributeType;

	public static final String APPLICATION_NAME_COLUMN_NAME = "application_name";

	public static final String ATTRIBUTE_TYPE_NAME_COLUMN_NAME = "attribute_type_name";

	public AttributeProviderEntity() {
		// empty
	}

	public AttributeProviderEntity(ApplicationEntity application,
			AttributeTypeEntity attributeType) {
		this.application = application;
		this.attributeType = attributeType;
		this.pk = new AttributeProviderPK(application, attributeType);
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "applicationName", column = @Column(name = APPLICATION_NAME_COLUMN_NAME)),
			@AttributeOverride(name = "attributeTypeName", column = @Column(name = ATTRIBUTE_TYPE_NAME_COLUMN_NAME)) })
	public AttributeProviderPK getPk() {
		return this.pk;
	}

	public void setPk(AttributeProviderPK pk) {
		this.pk = pk;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = APPLICATION_NAME_COLUMN_NAME, insertable = false, updatable = false)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = ATTRIBUTE_TYPE_NAME_COLUMN_NAME, insertable = false, updatable = false)
	public AttributeTypeEntity getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(AttributeTypeEntity attributeType) {
		this.attributeType = attributeType;
	}

	@Transient
	public String getApplicationName() {
		return this.pk.getApplicationName();
	}

	@Transient
	public String getAttributeTypeName() {
		return this.pk.getAttributeTypeName();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof AttributeProviderEntity) {
			return false;
		}
		AttributeProviderEntity rhs = (AttributeProviderEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pk).toHashCode();
	}

	public static Query createQueryWhereAttributeType(
			EntityManager entityManager, AttributeTypeEntity attributeType) {
		Query query = entityManager
				.createNamedQuery(QUERY_WHERE_ATTRIBUTE_TYPE);
		query.setParameter("attributeType", attributeType);
		return query;
	}
}
