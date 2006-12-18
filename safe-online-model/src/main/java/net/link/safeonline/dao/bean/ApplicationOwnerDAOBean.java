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
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class ApplicationOwnerDAOBean implements ApplicationOwnerDAO {

	private static final Log LOG = LogFactory
			.getLog(ApplicationOwnerDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public ApplicationOwnerEntity findApplicationOwner(String name) {
		LOG.debug("find app owner: " + name);
		ApplicationOwnerEntity applicationOwner = this.entityManager.find(
				ApplicationOwnerEntity.class, name);
		return applicationOwner;
	}

	public void addApplicationOwner(String name, SubjectEntity admin) {
		LOG.debug("add application owner: " + name);
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				name, admin);
		this.entityManager.persist(applicationOwner);
	}

	public ApplicationOwnerEntity getApplicationOwner(String name)
			throws ApplicationOwnerNotFoundException {
		LOG.debug("get app owner: " + name);
		ApplicationOwnerEntity applicationOwner = findApplicationOwner(name);
		if (null == applicationOwner) {
			throw new ApplicationOwnerNotFoundException();
		}
		return applicationOwner;
	}

	@SuppressWarnings("unchecked")
	public List<ApplicationOwnerEntity> getApplicationOwners() {
		Query query = ApplicationOwnerEntity.createQueryAll(this.entityManager);
		List<ApplicationOwnerEntity> applicationOwners = query.getResultList();
		return applicationOwners;
	}

	public ApplicationOwnerEntity getApplicationOwner(SubjectEntity adminSubject) {
		Query query = ApplicationOwnerEntity.createQueryWhereAdmin(
				this.entityManager, adminSubject);
		ApplicationOwnerEntity applicationOwner = (ApplicationOwnerEntity) query
				.getSingleResult();
		return applicationOwner;
	}
}
