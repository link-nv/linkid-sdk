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
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = SubjectDAO.JNDI_BINDING)
public class SubjectDAOBean implements SubjectDAO {

    private static final Log             LOG = LogFactory.getLog(SubjectDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                entityManager;

    private SubjectEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, SubjectEntity.QueryInterface.class);
    }

    public SubjectEntity findSubject(String userId) {

        LOG.debug("find subject: " + userId);
        SubjectEntity subject = this.entityManager.find(SubjectEntity.class, userId);
        return subject;
    }

    public SubjectEntity addSubject(String userId) {

        LOG.debug("add subject: " + userId);
        SubjectEntity subject = new SubjectEntity(userId);
        this.entityManager.persist(subject);
        return subject;
    }

    public SubjectEntity getSubject(String userId)
            throws SubjectNotFoundException {

        LOG.debug("get subject: " + userId);
        SubjectEntity subject = findSubject(userId);
        if (null == subject)
            throw new SubjectNotFoundException();
        return subject;
    }

    public void removeSubject(SubjectEntity subject) {

        this.entityManager.remove(subject);
    }

    public List<String> listUsers() {

        List<String> users = this.queryObject.listUsers();
        return users;
    }
}
