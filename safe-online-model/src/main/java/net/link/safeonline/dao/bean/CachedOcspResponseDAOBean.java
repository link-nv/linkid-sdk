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
import net.link.safeonline.dao.CachedOcspResponseDAO;
import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;

@Stateless
public class CachedOcspResponseDAOBean implements CachedOcspResponseDAO {

	private static final Log LOG = LogFactory
			.getLog(CachedOcspResponseDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public CachedOcspResponseEntity addCachedOcspResponse(String key,
			boolean result, TrustDomainEntity trustDomain) {
		LOG.debug("adding ocsp response to cache: key " + key);

		CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(
				key, result, trustDomain);
		this.entityManager.persist(cachedOcspResponse);

		return cachedOcspResponse;
	}

	@SuppressWarnings("unchecked")
	public CachedOcspResponseEntity findCachedOcspResponse(String key) {
		LOG.debug("looking for cached ocsp response: key " + key);

		Query query = CachedOcspResponseEntity.createQueryWhereKey(
				entityManager, key);
		List<CachedOcspResponseEntity> result = query.getResultList();
		if (result.isEmpty()) {
			LOG.debug("ocsp cache miss for key: " + key);
			return null;
		}
		LOG.debug("ocsp cache hit for key: " + key);
		return result.get(0);

	}

	public void removeCachedOcspResponse(
			CachedOcspResponseEntity cachedOcspResponse) {
		LOG.debug("removing ocsp response from cache for key: "
				+ cachedOcspResponse.getKey());

		this.entityManager.remove(cachedOcspResponse);
	}

	public void clearOcspCache() {
		LOG.debug("clearing ocsp response cache for all trust domains");

		Query query = CachedOcspResponseEntity
				.createQueryDeleteAll(entityManager);
		query.executeUpdate();
	}

	public void clearOcspCachePerTrustDomain(TrustDomainEntity trustDomain) {
		LOG.debug("clearing ocsp cache for trust domain: "
				+ trustDomain.getName());

		Query query = CachedOcspResponseEntity.createQueryDeletePerDomain(
				entityManager, trustDomain);
		query.executeUpdate();
	}

	public void clearOcspCacheExpiredForTrustDomain(
			TrustDomainEntity trustDomain) {
		LOG.debug("clearing expired ocsp cache entries");

		Query query = CachedOcspResponseEntity.createQueryDeleteExpired(
				this.entityManager, trustDomain);
		query.executeUpdate();
	}

}
