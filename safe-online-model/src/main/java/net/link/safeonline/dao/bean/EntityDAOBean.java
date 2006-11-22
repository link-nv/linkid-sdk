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

	public EntityEntity findEntity(String login) {
		LOG.debug("find entity: " + login);
		EntityEntity entity = this.entityManager
				.find(EntityEntity.class, login);
		return entity;
	}

	public EntityEntity addEntity(String login, String password) {
		EntityEntity entity = addEntity(login, password, null);
		return entity;
	}

	public EntityEntity addEntity(String login, String password, String name) {
		LOG.debug("add entity: " + login);
		EntityEntity entity = new EntityEntity(login, password, name);
		this.entityManager.persist(entity);
		return entity;
	}
}
