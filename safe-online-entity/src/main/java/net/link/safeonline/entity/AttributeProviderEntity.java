/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.AttributeProviderEntity.DELETE_WHERE_APPLICATION;
import static net.link.safeonline.entity.AttributeProviderEntity.DELETE_WHERE_ATTRIBUTE_TYPE;
import static net.link.safeonline.entity.AttributeProviderEntity.QUERY_WHERE_ATTRIBUTE_TYPE;

import java.io.Serializable;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Definition of the attribute provider entity. This entity manages the write
 * and unconfirmed read access control of applications towards attributes.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "attribute_provider")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_ATTRIBUTE_TYPE, query = "SELECT attributeProvider FROM AttributeProviderEntity AS attributeProvider "
				+ "WHERE attributeProvider.attributeType = :attributeType"),
		@NamedQuery(name = DELETE_WHERE_APPLICATION, query = "DELETE FROM AttributeProviderEntity AS attributeProvider "
				+ "WHERE attributeProvider.application = :application"),
		@NamedQuery(name = DELETE_WHERE_ATTRIBUTE_TYPE, query = "DELETE FROM AttributeProviderEntity AS attributeProvider "
				+ "WHERE attributeProvider.attributeType = :attributeType") })
public class AttributeProviderEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_ATTRIBUTE_TYPE = "ape.at";

	public static final String DELETE_WHERE_APPLICATION = "ape.del.app";

	public static final String DELETE_WHERE_ATTRIBUTE_TYPE = "ape.del.attr";

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

	public interface QueryInterface {
		@QueryMethod(QUERY_WHERE_ATTRIBUTE_TYPE)
		List<AttributeProviderEntity> listAttributeProviders(
				@QueryParam("attributeType")
				AttributeTypeEntity attributeType);

		@UpdateMethod(DELETE_WHERE_APPLICATION)
		int removeAttributeProviders(@QueryParam("application")
		ApplicationEntity application);

		@UpdateMethod(DELETE_WHERE_ATTRIBUTE_TYPE)
		int removeAttributeProviders(@QueryParam("attributeType")
		AttributeTypeEntity attributeType);
	}
}
