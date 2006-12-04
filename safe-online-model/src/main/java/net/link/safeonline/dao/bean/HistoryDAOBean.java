package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.HistoryEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class HistoryDAOBean implements HistoryDAO {

	private static final Log LOG = LogFactory.getLog(HistoryDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public void addHistoryEntry(Date when, SubjectEntity subject, String event) {
		LOG.debug("add history entry: " + when + "; subject: "
				+ subject.getLogin() + "; event: " + event);
		HistoryEntity history = new HistoryEntity(when, subject, event);
		this.entityManager.persist(history);
	}

	@SuppressWarnings("unchecked")
	public List<HistoryEntity> getHistory(SubjectEntity subject) {
		LOG.debug("get history for entity: " + subject.getLogin());
		Query query = HistoryEntity.createQueryWhereEntity(this.entityManager,
				subject);
		List<HistoryEntity> result = query.getResultList();
		return result;
	}
}
