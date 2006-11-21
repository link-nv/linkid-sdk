package net.link.safeonline.dao.bean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class ApplicationDAOBean implements ApplicationDAO {

	private static final Log LOG = LogFactory.getLog(ApplicationDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public ApplicationEntity findApplication(String applicationName) {
		LOG.debug("find application: " + applicationName);
		ApplicationEntity application = this.entityManager.find(
				ApplicationEntity.class, applicationName);
		return application;
	}

	public void addApplication(String applicationName) {
		ApplicationEntity application = new ApplicationEntity(applicationName);
		this.entityManager.persist(application);
	}
}
