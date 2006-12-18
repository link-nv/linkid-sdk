/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import net.link.safeonline.entity.listener.SecurityApplicationEntityListener;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_ALL;
import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_OWNER;

@Entity
@Table(name = "application")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_ALL, query = "FROM ApplicationEntity"),
		@NamedQuery(name = QUERY_WHERE_OWNER, query = "SELECT application "
				+ "FROM ApplicationEntity AS application "
				+ "WHERE application.applicationOwner = :applicationOwner") })
@EntityListeners(SecurityApplicationEntityListener.class)
public class ApplicationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_ALL = "app.all";

	public static final String QUERY_WHERE_OWNER = "app.owner";

	String name;

	String description;

	boolean allowUserSubscription;

	boolean removable;

	private ApplicationOwnerEntity applicationOwner;

	public ApplicationEntity() {
		// empty
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner) {
		this(name, applicationOwner, true);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner, String description) {
		this(name, applicationOwner, description, true, true);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner,
			boolean allowUserSubscription) {
		this(name, applicationOwner, true, true);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner,
			boolean allowUserSubscription, boolean removable) {
		this(name, applicationOwner, null, allowUserSubscription, removable);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner, String description,
			boolean allowUserSubscription, boolean removable) {
		this.name = name;
		this.applicationOwner = applicationOwner;
		this.description = description;
		this.allowUserSubscription = allowUserSubscription;
		this.removable = removable;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Id
	@Column(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAllowUserSubscription() {
		return this.allowUserSubscription;
	}

	public void setAllowUserSubscription(boolean allowUserSubscription) {
		this.allowUserSubscription = allowUserSubscription;
	}

	public boolean isRemovable() {
		return this.removable;
	}

	public void setRemovable(boolean removable) {
		this.removable = removable;
	}

	@ManyToOne(optional = false)
	public ApplicationOwnerEntity getApplicationOwner() {
		return this.applicationOwner;
	}

	public void setApplicationOwner(ApplicationOwnerEntity applicationOwner) {
		this.applicationOwner = applicationOwner;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof ApplicationEntity) {
			return false;
		}
		ApplicationEntity rhs = (ApplicationEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(
				"name", this.name).append("description", this.description)
				.append("allowUserSubscription", this.allowUserSubscription)
				.append("removable", this.removable).toString();
	}

	public static Query createQueryAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_ALL);
		return query;
	}

	public static Query createQueryWhereApplicationOwner(
			EntityManager entityManager, ApplicationOwnerEntity applicationOwner) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_OWNER);
		query.setParameter("applicationOwner", applicationOwner);
		return query;
	}
}
