/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class ApplicationIdentityDAOBean implements ApplicationIdentityDAO {

    private static final Log                         LOG = LogFactory.getLog(ApplicationIdentityDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                            entityManager;

    private ApplicationIdentityEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                ApplicationIdentityEntity.QueryInterface.class);
    }

    public ApplicationIdentityEntity addApplicationIdentity(ApplicationEntity application, long identityVersion) {

        LOG.debug("add application identity: " + application.getName() + ", version: " + identityVersion);
        ApplicationIdentityEntity applicationIdentity = new ApplicationIdentityEntity(application, identityVersion);
        this.entityManager.persist(applicationIdentity);
        return applicationIdentity;
    }

    public ApplicationIdentityEntity getApplicationIdentity(ApplicationEntity application, long identityVersion)
            throws ApplicationIdentityNotFoundException {

        ApplicationIdentityPK applicationIdentityPK = new ApplicationIdentityPK(application.getName(), identityVersion);
        ApplicationIdentityEntity applicationIdentity = this.entityManager.find(ApplicationIdentityEntity.class,
                applicationIdentityPK);
        if (null == applicationIdentity)
            throw new ApplicationIdentityNotFoundException();
        return applicationIdentity;
    }

    public List<ApplicationIdentityEntity> listApplicationIdentities(ApplicationEntity application) {

        List<ApplicationIdentityEntity> applicationIdentities = this.queryObject.listApplicationIdentities(application);
        return applicationIdentities;
    }

    public void removeApplicationIdentity(ApplicationIdentityEntity applicationIdentity) {

        this.entityManager.remove(applicationIdentity);
    }

    public ApplicationIdentityAttributeEntity addApplicationIdentityAttribute(
            ApplicationIdentityEntity applicationIdentity, AttributeTypeEntity attributeType, boolean required,
            boolean dataMining) {

        LOG.debug("add application identity attribute: " + attributeType.getName() + " to application "
                + applicationIdentity.getApplication().getName() + "; id version: "
                + applicationIdentity.getIdentityVersion());
        ApplicationIdentityAttributeEntity applicationIdentityAttribute = new ApplicationIdentityAttributeEntity(
                applicationIdentity, attributeType, required, dataMining);
        this.entityManager.persist(applicationIdentityAttribute);
        /*
         * Update both sides of the relationship.
         */
        applicationIdentity.getAttributes().add(applicationIdentityAttribute);
        return applicationIdentityAttribute;
    }

    public void removeApplicationIdentityAttribute(ApplicationIdentityAttributeEntity applicationIdentityAttribute) {

        applicationIdentityAttribute.getApplicationIdentity().getAttributes().remove(applicationIdentityAttribute);
        this.entityManager.remove(applicationIdentityAttribute);
    }

    public List<ApplicationIdentityEntity> listApplicationIdentities() {

        return this.queryObject.listApplicationIdentities();
    }
}
