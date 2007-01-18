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
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.entity.TrustDomainEntity;

@Stateless
public class TrustDomainDAOBean implements TrustDomainDAO {

	private static final Log LOG = LogFactory.getLog(TrustDomainDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<TrustDomainEntity> getTrustDomains() {
		LOG.debug("get trust domains");
		Query query = TrustDomainEntity.createQueryAll(this.entityManager);
		List<TrustDomainEntity> trustDomains = query.getResultList();
		return trustDomains;
	}

	public void addTrustDomain(String name) {
		LOG.debug("add trust domain: " + name);
		TrustDomainEntity trustDomain = new TrustDomainEntity(name);
		this.entityManager.persist(trustDomain);
	}

	@SuppressWarnings("unchecked")
	public TrustDomainEntity findTrustDomain(String name) {
		LOG.debug("find trust domain: " + name);
		Query query = TrustDomainEntity.createQueryWhereName(
				this.entityManager, name);
		List<TrustDomainEntity> trustDomains = query.getResultList();
		if (trustDomains.isEmpty()) {
			return null;
		}
		return trustDomains.get(0);
	}

	public void removeTrustDomain(TrustDomainEntity trustDomain) {
		LOG.debug("remove trust domain: " + trustDomain.getName());
		this.entityManager.remove(trustDomain);
	}

	public TrustDomainEntity getTrustDomain(String name)
			throws TrustDomainNotFoundException {
		LOG.debug("get trust domain: " + name);
		TrustDomainEntity trustDomain = findTrustDomain(name);
		if (null == trustDomain) {
			throw new TrustDomainNotFoundException();
		}
		return trustDomain;
	}
}
