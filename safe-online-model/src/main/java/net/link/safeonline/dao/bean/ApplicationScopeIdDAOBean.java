/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.ApplicationScopeIdDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationScopeIdEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.model.IdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = ApplicationScopeIdDAO.JNDI_BINDING)
public class ApplicationScopeIdDAOBean implements ApplicationScopeIdDAO {

    private static final Log                        LOG = LogFactory.getLog(ApplicationScopeIdDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                           entityManager;

    private ApplicationScopeIdEntity.QueryInterface queryObject;

    @EJB(mappedName = IdGenerator.JNDI_BINDING)
    private IdGenerator                             idGenerator;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, ApplicationScopeIdEntity.QueryInterface.class);
    }

    public ApplicationScopeIdEntity addApplicationScopeId(SubjectEntity subject, ApplicationEntity application) {

        String id = this.idGenerator.generateId();
        LOG.debug("add application scope id=" + id + " for " + subject.getUserId() + " and application " + application.getName());
        ApplicationScopeIdEntity applicationScopeId = new ApplicationScopeIdEntity(subject, id, application);
        this.entityManager.persist(applicationScopeId);
        return applicationScopeId;
    }

    public ApplicationScopeIdEntity findApplicationScopeId(SubjectEntity subject, ApplicationEntity application) {

        LOG.debug("find application scope id for " + subject.getUserId() + " application=" + application.getName());
        return this.queryObject.findApplicationScopeId(subject, application);
    }

    public ApplicationScopeIdEntity findApplicationScopeId(String id) {

        LOG.debug("find application scope id: id=" + id);
        ApplicationScopeIdEntity applicationScopeId = this.entityManager.find(ApplicationScopeIdEntity.class, id);
        return applicationScopeId;
    }

    public void removeApplicationScopeIds(SubjectEntity subject) {

        LOG.debug("remove application scope id's for subject: " + subject.getUserId());
        this.queryObject.deleteAll(subject);
    }

    public void removeApplicationScopeIds(ApplicationEntity application) {

        LOG.debug("remove application scope id's for application: " + application);
        this.queryObject.deleteAll(application);
    }
}
