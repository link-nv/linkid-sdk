/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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

	private List<AttributeTypeEntity> attributeTypes;

	public ApplicationIdentityEntity() {
		// empty
	}

	public ApplicationIdentityEntity(ApplicationEntity application,
			long identityVersion, List<AttributeTypeEntity> attributeTypes) {
		this.pk = new ApplicationIdentityPK(application.getName(),
				identityVersion);
		this.application = application;
		this.attributeTypes = attributeTypes;
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "application", column = @Column(name = "application")),
			@AttributeOverride(name = "identityVersion", column = @Column(name = "identityVersion")) })
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
	@JoinColumn(name = "application", insertable = false, updatable = false)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	public List<AttributeTypeEntity> getAttributeTypes() {
		return this.attributeTypes;
	}

	public void setAttributeTypes(List<AttributeTypeEntity> attributeTypes) {
		this.attributeTypes = attributeTypes;
	}

	public static Query createQueryWhereApplication(
			EntityManager entityManager, ApplicationEntity application) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_APPLICATION);
		query.setParameter("application", application);
		return query;
	}
}
