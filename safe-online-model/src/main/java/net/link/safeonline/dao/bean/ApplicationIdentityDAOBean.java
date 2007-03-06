/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.AttributeTypeEntity;

@Stateless
public class ApplicationIdentityDAOBean implements ApplicationIdentityDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public void addApplicationIdentity(ApplicationEntity application,
			long identityVersion, List<AttributeTypeEntity> attributeTypes) {
		ApplicationIdentityEntity applicationIdentity = new ApplicationIdentityEntity(
				application, identityVersion, attributeTypes);
		this.entityManager.persist(applicationIdentity);
	}

	public ApplicationIdentityEntity getApplicationIdentity(
			ApplicationEntity application, long identityVersion)
			throws ApplicationIdentityNotFoundException {
		ApplicationIdentityPK applicationIdentityPK = new ApplicationIdentityPK(
				application.getName(), identityVersion);
		ApplicationIdentityEntity applicationIdentity = this.entityManager
				.find(ApplicationIdentityEntity.class, applicationIdentityPK);
		if (null == applicationIdentity) {
			throw new ApplicationIdentityNotFoundException();
		}
		return applicationIdentity;
	}

	@SuppressWarnings("unchecked")
	public List<ApplicationIdentityEntity> getApplicationIdentities(
			ApplicationEntity application) {
		Query query = ApplicationIdentityEntity.createQueryWhereApplication(
				this.entityManager, application);
		List<ApplicationIdentityEntity> applicationIdentities = query
				.getResultList();
		return applicationIdentities;
	}

	public void removeApplicationIdentity(
			ApplicationIdentityEntity applicationIdentity) {
		this.entityManager.remove(applicationIdentity);
	}
}
