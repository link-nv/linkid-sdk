/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
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
		LOG.debug("adding application: " + applicationName);
		ApplicationEntity application = new ApplicationEntity(applicationName);
		this.entityManager.persist(application);
	}

	@SuppressWarnings("unchecked")
	public List<ApplicationEntity> getApplications() {
		Query query = ApplicationEntity.createQueryAll(this.entityManager);
		List<ApplicationEntity> applications = query.getResultList();
		return applications;
	}

	public ApplicationEntity getApplication(String applicationName)
			throws ApplicationNotFoundException {
		ApplicationEntity application = findApplication(applicationName);
		if (null == application) {
			throw new ApplicationNotFoundException();
		}
		return application;
	}

	public void addApplication(String applicationName,
			boolean allowUserSubscription) {
		LOG.debug("adding application: " + applicationName);
		ApplicationEntity application = new ApplicationEntity(applicationName,
				allowUserSubscription);
		this.entityManager.persist(application);
	}

	public void addApplication(ApplicationEntity application) {
		LOG.debug("adding application: " + application.getName());
		this.entityManager.persist(application);
	}

	public void removeApplication(ApplicationEntity application) {
		LOG.debug("remove application: " + application.getName());
		this.entityManager.remove(application);
	}
}
