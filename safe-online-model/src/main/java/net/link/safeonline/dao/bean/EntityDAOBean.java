package net.link.safeonline.dao.bean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.entity.EntityEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class EntityDAOBean implements EntityDAO {

	private static final Log LOG = LogFactory.getLog(EntityDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public EntityEntity findEntity(String username) {
		LOG.debug("find entity: " + username);
		EntityEntity entity = this.entityManager.find(EntityEntity.class,
				username);
		return entity;
	}

	public void addEntity(String username, String password) {
		LOG.debug("add entity: " + username);
		EntityEntity entity = new EntityEntity(username, password);
		this.entityManager.persist(entity);
	}
}
