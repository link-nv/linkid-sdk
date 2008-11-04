/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = TrustDomainDAO.JNDI_BINDING)
public class TrustDomainDAOBean implements TrustDomainDAO {

    private static final Log                 LOG = LogFactory.getLog(TrustDomainDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                    entityManager;

    private TrustDomainEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, TrustDomainEntity.QueryInterface.class);
    }

    public List<TrustDomainEntity> listTrustDomains() {

        LOG.debug("get trust domains");
        List<TrustDomainEntity> trustDomains = this.queryObject.listTrustDomains();
        return trustDomains;
    }

    public TrustDomainEntity addTrustDomain(String name, boolean performOcspCheck) {

        LOG.debug("add trust domain: " + name);
        TrustDomainEntity trustDomain = new TrustDomainEntity(name, performOcspCheck);
        this.entityManager.persist(trustDomain);
        return trustDomain;
    }

    public TrustDomainEntity addTrustDomain(String name, boolean performOcspCheck, long ocspCacheTimeOutMillis) {

        LOG.debug("add trust domain: " + name);
        TrustDomainEntity trustDomain = new TrustDomainEntity(name, performOcspCheck, ocspCacheTimeOutMillis);
        this.entityManager.persist(trustDomain);
        return trustDomain;
    }

    public TrustDomainEntity findTrustDomain(String name) {

        LOG.debug("find trust domain: " + name);
        TrustDomainEntity trustDomain = this.queryObject.findTrustDomain(name);
        return trustDomain;
    }

    public void removeTrustDomain(TrustDomainEntity trustDomain) {

        LOG.debug("remove trust domain: " + trustDomain.getName());
        this.entityManager.remove(trustDomain);
    }

    public TrustDomainEntity getTrustDomain(String name)
            throws TrustDomainNotFoundException {

        LOG.debug("get trust domain: " + name);
        TrustDomainEntity trustDomain = findTrustDomain(name);
        if (null == trustDomain)
            throw new TrustDomainNotFoundException();
        return trustDomain;
    }
}
