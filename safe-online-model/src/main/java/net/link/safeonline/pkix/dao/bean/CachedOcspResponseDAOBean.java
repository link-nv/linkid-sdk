/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.dao.bean;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.CachedOcspResultType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.pkix.dao.CachedOcspResponseDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = CachedOcspResponseDAO.JNDI_BINDING)
public class CachedOcspResponseDAOBean implements CachedOcspResponseDAO {

    private static final Log                        LOG = LogFactory.getLog(CachedOcspResponseDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                           entityManager;

    private CachedOcspResponseEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, CachedOcspResponseEntity.QueryInterface.class);
    }

    public CachedOcspResponseEntity addCachedOcspResponse(String key, CachedOcspResultType result, TrustDomainEntity trustDomain) {

        LOG.debug("adding ocsp response to cache: key " + key);

        CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(key, result, trustDomain);
        this.entityManager.persist(cachedOcspResponse);

        return cachedOcspResponse;
    }

    public CachedOcspResponseEntity findCachedOcspResponse(String key) {

        LOG.debug("looking for cached ocsp response: key " + key);
        CachedOcspResponseEntity result = this.queryObject.findCachedOcspResponse(key);
        if (null == result) {
            LOG.debug("ocsp cache miss for key: " + key);
        } else {
            LOG.debug("ocsp cache hit for key: " + key);
        }
        return result;

    }

    public void removeCachedOcspResponse(CachedOcspResponseEntity cachedOcspResponse) {

        LOG.debug("removing ocsp response from cache for key: " + cachedOcspResponse.getKey());

        this.entityManager.remove(cachedOcspResponse);
    }

    public void clearOcspCache() {

        LOG.debug("clearing ocsp response cache for all trust domains");
        this.queryObject.deleteAll();
    }

    public void clearOcspCachePerTrustDomain(TrustDomainEntity trustDomain) {

        LOG.debug("clearing ocsp cache for trust domain: " + trustDomain.getName());
        this.queryObject.deletePerDomain(trustDomain);
    }

    public void clearOcspCacheExpiredForTrustDomain(TrustDomainEntity trustDomain) {

        LOG.debug("clearing expired ocsp cache entries");
        this.queryObject.deleteExpired(trustDomain, new Date(System.currentTimeMillis() - trustDomain.getOcspCacheTimeOutMillis()));
    }
}
