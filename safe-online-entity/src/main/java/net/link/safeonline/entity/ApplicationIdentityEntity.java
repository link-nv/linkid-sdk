/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import static net.link.safeonline.entity.ApplicationIdentityEntity.QUERY_WHERE_APPLICATION;

@Entity
@Table(name = "application_identity")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT applicationIdentity "
		+ "FROM ApplicationIdentityEntity AS applicationIdentity "
		+ "WHERE applicationIdentity.application = :application") })
public class ApplicationIdentityEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_APPLICATION = "ai.app";

	private ApplicationIdentityPK pk;

	private ApplicationEntity application;

	private List<ApplicationIdentityAttributeEntity> attributes;

	public ApplicationIdentityEntity() {
		this.attributes = new LinkedList<ApplicationIdentityAttributeEntity>();
	}

	public ApplicationIdentityEntity(ApplicationEntity application,
			long identityVersion) {
		this.pk = new ApplicationIdentityPK(application.getName(),
				identityVersion);
		this.application = application;
		this.attributes = new LinkedList<ApplicationIdentityAttributeEntity>();
	}

	public static final String APPLICATION_COLUMN_NAME = "application";

	public static final String IDENTITY_VERSION_COLUMN_NAME = "identityVersion";

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "application", column = @Column(name = APPLICATION_COLUMN_NAME)),
			@AttributeOverride(name = "identityVersion", column = @Column(name = IDENTITY_VERSION_COLUMN_NAME)) })
	public ApplicationIdentityPK getPk() {
		return this.pk;
	}

	public void setPk(ApplicationIdentityPK pk) {
		this.pk = pk;
	}

	@Transient
	public long getIdentityVersion() {
		return this.pk.getIdentityVersion();
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = APPLICATION_COLUMN_NAME, insertable = false, updatable = false)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "applicationIdentity")
	public List<ApplicationIdentityAttributeEntity> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(
			List<ApplicationIdentityAttributeEntity> attributes) {
		this.attributes = attributes;
	}

	@Transient
	public List<AttributeTypeEntity> getAttributeTypes() {
		List<AttributeTypeEntity> attributeTypes = new LinkedList<AttributeTypeEntity>();
		for (ApplicationIdentityAttributeEntity attribute : this
				.getAttributes()) {
			attributeTypes.add(attribute.getAttributeType());
		}
		return attributeTypes;
	}

	@Transient
	public List<AttributeTypeEntity> getRequiredAttributeTypes() {
		List<AttributeTypeEntity> requiredAttributeTypes = new LinkedList<AttributeTypeEntity>();
		for (ApplicationIdentityAttributeEntity attribute : this
				.getAttributes()) {
			/*
			 * This could be optimized via an SQL query, though the identity
			 * attribute set will always be limited.
			 */
			if (false == attribute.isRequired()) {
				continue;
			}
			requiredAttributeTypes.add(attribute.getAttributeType());
		}
		return requiredAttributeTypes;
	}

	public static Query createQueryWhereApplication(
			EntityManager entityManager, ApplicationEntity application) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_APPLICATION);
		query.setParameter("application", application);
		return query;
	}
}
