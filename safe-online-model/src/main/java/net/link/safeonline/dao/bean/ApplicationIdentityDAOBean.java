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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.AttributeTypeEntity;

@Stateless
public class ApplicationIdentityDAOBean implements ApplicationIdentityDAO {

	private static final Log LOG = LogFactory
			.getLog(ApplicationIdentityDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public void addApplicationIdentity(ApplicationEntity application,
			long identityVersion, List<AttributeTypeEntity> attributeTypes) {
		LOG.debug("add application identity: " + application.getName()
				+ ", version: " + identityVersion);
		if (null != attributeTypes) {
			for (AttributeTypeEntity attributeType : attributeTypes) {
				LOG.debug("identity attribute: " + attributeType.getName());
			}
		}
		ApplicationIdentityEntity applicationIdentity = new ApplicationIdentityEntity(
				application, identityVersion, attributeTypes);
		this.entityManager.persist(applicationIdentity);
		LOG.debug("flushing the persistence context");
		this.entityManager.flush();
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
	public List<ApplicationIdentityEntity> listApplicationIdentities(
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
