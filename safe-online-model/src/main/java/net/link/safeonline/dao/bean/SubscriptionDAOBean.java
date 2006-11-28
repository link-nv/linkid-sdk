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
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class SubscriptionDAOBean implements SubscriptionDAO {

	private static final Log LOG = LogFactory.getLog(SubscriptionDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public SubscriptionEntity findSubscription(EntityEntity entity,
			ApplicationEntity application) {
		LOG.debug("find subscription for: " + entity.getLogin() + " to "
				+ application.getName());
		Query query = SubscriptionEntity.createQueryWhereEntityAndApplication(
				this.entityManager, entity, application);
		List resultList = query.getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		SubscriptionEntity subscription = (SubscriptionEntity) resultList
				.get(0);
		return subscription;
	}

	public void addSubscription(SubscriptionOwnerType subscriptionOwnerType,
			EntityEntity entity, ApplicationEntity application) {
		LOG.debug("add subscription for " + entity.getLogin() + " to "
				+ application.getName());
		SubscriptionEntity subscription = new SubscriptionEntity(
				subscriptionOwnerType, entity, application);
		this.entityManager.persist(subscription);
	}

	@SuppressWarnings("unchecked")
	public List<SubscriptionEntity> getSubsciptions(EntityEntity entity) {
		LOG.debug("get subscriptions for entity: " + entity.getLogin());
		Query query = SubscriptionEntity.createQueryWhereEntity(
				this.entityManager, entity);
		List<SubscriptionEntity> subscriptions = query.getResultList();
		return subscriptions;
	}

	public void removeSubscription(EntityEntity entity,
			ApplicationEntity application) throws SubscriptionNotFoundException {
		SubscriptionEntity subscription = findSubscription(entity, application);
		if (null == subscription) {
			throw new SubscriptionNotFoundException();
		}
		this.entityManager.remove(subscription);
	}
}
