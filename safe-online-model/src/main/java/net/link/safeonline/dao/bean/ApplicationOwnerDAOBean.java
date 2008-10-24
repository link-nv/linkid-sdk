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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class ApplicationOwnerDAOBean implements ApplicationOwnerDAO {

    private static final Log                      LOG = LogFactory.getLog(ApplicationOwnerDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                         entityManager;

    private ApplicationOwnerEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, ApplicationOwnerEntity.QueryInterface.class);
    }

    public ApplicationOwnerEntity findApplicationOwner(String name) {

        LOG.debug("find app owner: " + name);
        ApplicationOwnerEntity applicationOwner = this.entityManager.find(ApplicationOwnerEntity.class, name);
        return applicationOwner;
    }

    public ApplicationOwnerEntity addApplicationOwner(String name, SubjectEntity admin) {

        LOG.debug("add application owner: " + name);
        ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(name, admin);
        this.entityManager.persist(applicationOwner);
        return applicationOwner;
    }

    public ApplicationOwnerEntity getApplicationOwner(String name) throws ApplicationOwnerNotFoundException {

        LOG.debug("get app owner: " + name);
        ApplicationOwnerEntity applicationOwner = findApplicationOwner(name);
        if (null == applicationOwner)
            throw new ApplicationOwnerNotFoundException();
        return applicationOwner;
    }

    public List<ApplicationOwnerEntity> listApplicationOwners() {

        List<ApplicationOwnerEntity> applicationOwners = this.queryObject.listApplicationOwners();
        return applicationOwners;
    }

    public ApplicationOwnerEntity getApplicationOwner(SubjectEntity adminSubject) throws ApplicationOwnerNotFoundException {

        ApplicationOwnerEntity applicationOwner = findApplicationOwner(adminSubject);
        if (null == applicationOwner)
            throw new ApplicationOwnerNotFoundException();
        return applicationOwner;
    }

    public ApplicationOwnerEntity findApplicationOwner(SubjectEntity adminSubject) {

        ApplicationOwnerEntity applicationOwner = this.queryObject.getApplicationOwner(adminSubject);
        return applicationOwner;
    }

    public void removeApplicationOwner(String name) {

        LOG.debug("remove application owner: " + name);
        ApplicationOwnerEntity applicationOwner = findApplicationOwner(name);
        this.entityManager.remove(applicationOwner);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void removeApplication(ApplicationEntity application) {

        LOG.debug("remove application " + application.getName() + " from owner: " + application.getApplicationOwner().getName());
        ApplicationOwnerEntity applicationOwner = findApplicationOwner(application.getApplicationOwner().getName());
        applicationOwner.getApplications().remove(application);
    }
}
