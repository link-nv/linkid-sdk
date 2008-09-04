/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.dao.ApplicationPoolDAO;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class ApplicationPoolDAOBean implements ApplicationPoolDAO {

    private static final Log                     LOG = LogFactory.getLog(ApplicationPoolDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                        entityManager;

    private ApplicationPoolEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                ApplicationPoolEntity.QueryInterface.class);
    }

    public ApplicationPoolEntity findApplicationPool(String applicationPoolName) {

        LOG.debug("find application pool: " + applicationPoolName);
        ApplicationPoolEntity applicationPool = this.entityManager.find(ApplicationPoolEntity.class,
                applicationPoolName);
        return applicationPool;
    }

    public ApplicationPoolEntity addApplicationPool(String applicationPoolName, long ssoTimeout) {

        LOG.debug("adding application: " + applicationPoolName);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, ssoTimeout);
        this.entityManager.persist(applicationPool);
        return applicationPool;
    }

    public List<ApplicationPoolEntity> listApplicationPools() {

        List<ApplicationPoolEntity> applicationPools = this.queryObject.listSsoPools();
        return applicationPools;
    }

    public ApplicationPoolEntity getApplicationPool(String applicationPoolName) throws ApplicationPoolNotFoundException {

        ApplicationPoolEntity applicationPool = findApplicationPool(applicationPoolName);
        if (null == applicationPool) {
            throw new ApplicationPoolNotFoundException();
        }
        return applicationPool;
    }

    public void removeApplicationPool(ApplicationPoolEntity applicationPool) {

        LOG.debug("remove application pool (DAO): " + applicationPool.getName());
        this.entityManager.remove(applicationPool);
    }
}
