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
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = ApplicationPoolDAO.JNDI_BINDING)
public class ApplicationPoolDAOBean implements ApplicationPoolDAO {

    private static final Log                     LOG = LogFactory.getLog(ApplicationPoolDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                        entityManager;

    private ApplicationPoolEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, ApplicationPoolEntity.QueryInterface.class);
    }

    public ApplicationPoolEntity findApplicationPool(String applicationPoolName) {

        LOG.debug("find application pool: " + applicationPoolName);
        ApplicationPoolEntity applicationPool = entityManager.find(ApplicationPoolEntity.class, applicationPoolName);
        return applicationPool;
    }

    public ApplicationPoolEntity addApplicationPool(String applicationPoolName, long ssoTimeout) {

        LOG.debug("adding application: " + applicationPoolName);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, ssoTimeout);
        entityManager.persist(applicationPool);
        return applicationPool;
    }

    public List<ApplicationPoolEntity> listApplicationPools() {

        List<ApplicationPoolEntity> applicationPools = queryObject.listApplicationPools();
        return applicationPools;
    }

    public ApplicationPoolEntity getApplicationPool(String applicationPoolName)
            throws ApplicationPoolNotFoundException {

        ApplicationPoolEntity applicationPool = findApplicationPool(applicationPoolName);
        if (null == applicationPool)
            throw new ApplicationPoolNotFoundException();
        return applicationPool;
    }

    public void removeApplicationPool(ApplicationPoolEntity applicationPool) {

        LOG.debug("remove application pool (DAO): " + applicationPool.getName());
        entityManager.remove(applicationPool);
    }

    public List<ApplicationPoolEntity> listCommonApplicationPools(ApplicationEntity application1, ApplicationEntity application2) {

        List<ApplicationPoolEntity> applicationPools = queryObject.listCommonApplicationPools(application1, application2);
        return applicationPools;
    }

    public List<ApplicationPoolEntity> listApplicationPools(ApplicationEntity application) {

        List<ApplicationPoolEntity> applicationPools = queryObject.listApplicationPools(application);
        return applicationPools;
    }

}
