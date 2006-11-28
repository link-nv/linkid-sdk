package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.SubscriptionEntity.QUERY_WHERE_ENTITY_AND_APPLICATION;
import static net.link.safeonline.entity.SubscriptionEntity.QUERY_WHERE_ENTITY;

@Entity
@Table(name = "subscription")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_ENTITY_AND_APPLICATION, query = "SELECT subscription FROM SubscriptionEntity AS subscription WHERE subscription.entity = :entity AND subscription.application = :application"),
		@NamedQuery(name = QUERY_WHERE_ENTITY, query = "SELECT subscription FROM SubscriptionEntity AS subscription WHERE subscription.entity = :entity") })
public class SubscriptionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_ENTITY_AND_APPLICATION = "sub.entity.app";

	public static final String QUERY_WHERE_ENTITY = "sub.entity";

	private long id;

	private EntityEntity entity;

	private ApplicationEntity application;

	private SubscriptionOwnerType subscriptionOwnerType;

	public SubscriptionEntity() {
		// empty
	}

	public SubscriptionEntity(SubscriptionOwnerType subscriptionOwnerType,
			EntityEntity entity, ApplicationEntity application) {
		this.subscriptionOwnerType = subscriptionOwnerType;
		this.entity = entity;
		this.application = application;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(optional = false)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@ManyToOne(optional = false)
	public EntityEntity getEntity() {
		return this.entity;
	}

	public void setEntity(EntityEntity entity) {
		this.entity = entity;
	}

	@Enumerated(EnumType.STRING)
	public SubscriptionOwnerType getSubscriptionOwnerType() {
		return subscriptionOwnerType;
	}

	public void setSubscriptionOwnerType(
			SubscriptionOwnerType applicationOwnerType) {
		this.subscriptionOwnerType = applicationOwnerType;
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
		return new EqualsBuilder().append(this.id, rhs.id).append(
				this.application, rhs.application).append(this.entity,
				rhs.entity).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("entity",
				this.entity).append("application", this.application).toString();
	}

	public static Query createQueryWhereEntityAndApplication(
			EntityManager entityManager, EntityEntity entity,
			ApplicationEntity application) {
		Query query = entityManager
				.createNamedQuery(QUERY_WHERE_ENTITY_AND_APPLICATION);
		query.setParameter("entity", entity);
		query.setParameter("application", application);
		return query;
	}

	public static Query createQueryWhereEntity(EntityManager entityManager,
			EntityEntity entity) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_ENTITY);
		query.setParameter("entity", entity);
		return query;
	}
}
