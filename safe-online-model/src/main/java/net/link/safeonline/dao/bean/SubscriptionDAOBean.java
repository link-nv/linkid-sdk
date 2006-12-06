package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class SubscriptionDAOBean implements SubscriptionDAO {

	private static final Log LOG = LogFactory.getLog(SubscriptionDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public SubscriptionEntity findSubscription(SubjectEntity subject,
			ApplicationEntity application) {
		LOG.debug("find subscription for: " + subject.getLogin() + " to "
				+ application.getName());
		Query query = SubscriptionEntity.createQueryWhereEntityAndApplication(
				this.entityManager, subject, application);
		List<SubscriptionEntity> resultList = query.getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		SubscriptionEntity subscription = resultList.get(0);
		return subscription;
	}

	public void addSubscription(SubscriptionOwnerType subscriptionOwnerType,
			SubjectEntity subject, ApplicationEntity application) {
		LOG.debug("add subscription for " + subject.getLogin() + " to "
				+ application.getName());
		SubscriptionEntity subscription = new SubscriptionEntity(
				subscriptionOwnerType, subject, application);
		this.entityManager.persist(subscription);
	}

	@SuppressWarnings("unchecked")
	public List<SubscriptionEntity> getSubsciptions(SubjectEntity subject) {
		LOG.debug("get subscriptions for subject: " + subject.getLogin());
		Query query = SubscriptionEntity.createQueryWhereEntity(
				this.entityManager, subject);
		List<SubscriptionEntity> subscriptions = query.getResultList();
		return subscriptions;
	}

	public void removeSubscription(SubjectEntity subject,
			ApplicationEntity application) throws SubscriptionNotFoundException {
		SubscriptionEntity subscription = findSubscription(subject, application);
		if (null == subscription) {
			throw new SubscriptionNotFoundException();
		}
		this.entityManager.remove(subscription);
	}

	public long getNumberOfSubscriptions(ApplicationEntity application) {
		Query query = SubscriptionEntity.createQueryCountWhereApplication(
				this.entityManager, application);
		Long countResult = (Long) query.getSingleResult();
		return countResult;
	}

	@SuppressWarnings("unchecked")
	public List<SubscriptionEntity> getSubscriptions(
			ApplicationEntity application) {
		LOG
				.debug("get subscriptions for application: "
						+ application.getName());
		Query query = SubscriptionEntity.createQueryWhereApplication(
				this.entityManager, application);
		List<SubscriptionEntity> subscriptions = query.getResultList();
		return subscriptions;
	}

	public void removeSubscription(SubscriptionEntity subscriptionEntity) {
		LOG.debug("remove subscription: " + subscriptionEntity.getId());
		this.entityManager.remove(subscriptionEntity);
	}
}
