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

import static net.link.safeonline.entity.SubscriptionEntity.QUERY_WHERE_SUBJECT_AND_APPLICATION;
import static net.link.safeonline.entity.SubscriptionEntity.QUERY_WHERE_SUBJECT;
import static net.link.safeonline.entity.SubscriptionEntity.QUERY_COUNT_WHERE_APPLICATION;

@Entity
@Table(name = "subscription")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_SUBJECT_AND_APPLICATION, query = "SELECT subscription FROM SubscriptionEntity AS subscription WHERE subscription.subject = :subject AND subscription.application = :application"),
		@NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT subscription FROM SubscriptionEntity AS subscription WHERE subscription.subject = :subject"),
		@NamedQuery(name = QUERY_COUNT_WHERE_APPLICATION, query = "SELECT COUNT(subscription) FROM SubscriptionEntity AS subscription WHERE subscription.application = :application") })
public class SubscriptionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_SUBJECT_AND_APPLICATION = "sub.subject.app";

	public static final String QUERY_WHERE_SUBJECT = "sub.subject";

	public static final String QUERY_COUNT_WHERE_APPLICATION = "sub.count.app";

	private long id;

	private SubjectEntity subject;

	private ApplicationEntity application;

	private SubscriptionOwnerType subscriptionOwnerType;

	public SubscriptionEntity() {
		// empty
	}

	public SubscriptionEntity(SubscriptionOwnerType subscriptionOwnerType,
			SubjectEntity subject, ApplicationEntity application) {
		this.subscriptionOwnerType = subscriptionOwnerType;
		this.subject = subject;
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
				this.application, rhs.application).append(this.subject,
				rhs.subject).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append(
				"subject", this.subject)
				.append("application", this.application).toString();
	}

	public static Query createQueryWhereEntityAndApplication(
			EntityManager entityManager, SubjectEntity subject,
			ApplicationEntity application) {
		Query query = entityManager
				.createNamedQuery(QUERY_WHERE_SUBJECT_AND_APPLICATION);
		query.setParameter("subject", subject);
		query.setParameter("application", application);
		return query;
	}

	public static Query createQueryWhereEntity(EntityManager entityManager,
			SubjectEntity subject) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_SUBJECT);
		query.setParameter("subject", subject);
		return query;
	}

	public static Query createQueryCountWhereApplication(
			EntityManager entityManager, ApplicationEntity application) {
		Query query = entityManager
				.createNamedQuery(QUERY_COUNT_WHERE_APPLICATION);
		query.setParameter("application", application);
		return query;
	}
}
