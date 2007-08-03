/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static net.link.safeonline.entity.SubscriptionEntity.QUERY_WHERE_SUBJECT;
import static net.link.safeonline.entity.SubscriptionEntity.QUERY_WHERE_APPLICATION;
import static net.link.safeonline.entity.SubscriptionEntity.QUERY_COUNT_WHERE_APPLICATION;
import static net.link.safeonline.entity.SubscriptionEntity.QUERY_COUNT_WHERE_APPLICATION_AND_ACTIVE;

@Entity
@Table(name = "subscription")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT subscription "
				+ "FROM SubscriptionEntity AS subscription "
				+ "WHERE subscription.subject = :subject"),
		@NamedQuery(name = QUERY_COUNT_WHERE_APPLICATION, query = "SELECT COUNT(*) "
				+ "FROM SubscriptionEntity AS subscription "
				+ "WHERE subscription.application = :application"),
		@NamedQuery(name = QUERY_COUNT_WHERE_APPLICATION_AND_ACTIVE, query = "SELECT COUNT(*) "
				+ "FROM SubscriptionEntity AS subscription "
				+ "WHERE subscription.application = :application "
				+ "AND subscription.lastLogin > :lastLogin"),
		@NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT subscription "
				+ "FROM SubscriptionEntity AS subscription "
				+ "WHERE subscription.application = :application") })
public class SubscriptionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_SUBJECT = "sub.subject";

	public static final String QUERY_COUNT_WHERE_APPLICATION = "sub.count.app";

	public static final String QUERY_COUNT_WHERE_APPLICATION_AND_ACTIVE = "sub.count.app.active";

	public static final String QUERY_WHERE_APPLICATION = "sub.application";

	private SubscriptionPK pk;

	private SubjectEntity subject;

	private ApplicationEntity application;

	private SubscriptionOwnerType subscriptionOwnerType;

	private Long confirmedIdentityVersion;

	private Date lastLogin;

	public SubscriptionEntity() {
		// empty
	}

	public SubscriptionEntity(SubscriptionOwnerType subscriptionOwnerType,
			SubjectEntity subject, ApplicationEntity application) {
		this.subscriptionOwnerType = subscriptionOwnerType;
		this.subject = subject;
		this.application = application;
		this.pk = new SubscriptionPK(subject, application);
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "application", column = @Column(name = "application")),
			@AttributeOverride(name = "subject", column = @Column(name = "subject")) })
	public SubscriptionPK getPk() {
		return this.pk;
	}

	public void setPk(SubscriptionPK pk) {
		this.pk = pk;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "application", insertable = false, updatable = false)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "subject", insertable = false, updatable = false)
	public SubjectEntity getSubject() {
		return this.subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
	}

	@Enumerated(EnumType.STRING)
	public SubscriptionOwnerType getSubscriptionOwnerType() {
		return subscriptionOwnerType;
	}

	public void setSubscriptionOwnerType(
			SubscriptionOwnerType applicationOwnerType) {
		this.subscriptionOwnerType = applicationOwnerType;
	}

	public Long getConfirmedIdentityVersion() {
		return this.confirmedIdentityVersion;
	}

	public void setConfirmedIdentityVersion(Long confirmedIdentityVersion) {
		this.confirmedIdentityVersion = confirmedIdentityVersion;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof SubscriptionEntity) {
			return false;
		}
		SubscriptionEntity rhs = (SubscriptionEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(
				"pk", this.pk).append("ownerType", this.subscriptionOwnerType)
				.toString();
	}

	public static Query createQueryCountWhereApplicationAndActive(
			EntityManager entityManager, ApplicationEntity application,
			long activeLimitInMillis) {
		Query query = entityManager
				.createNamedQuery(QUERY_COUNT_WHERE_APPLICATION_AND_ACTIVE);
		query.setParameter("application", application);
		query.setParameter("lastLogin", new Date(System.currentTimeMillis()
				- activeLimitInMillis));
		return query;
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_WHERE_SUBJECT)
		List<SubscriptionEntity> listSubsciptions(@QueryParam("subject")
		SubjectEntity subject);

		@QueryMethod(QUERY_COUNT_WHERE_APPLICATION)
		long getNumberOfSubscriptions(@QueryParam("application")
		ApplicationEntity application);

		@QueryMethod(QUERY_WHERE_APPLICATION)
		List<SubscriptionEntity> listSubscriptions(@QueryParam("application")
		ApplicationEntity application);
	}
}
