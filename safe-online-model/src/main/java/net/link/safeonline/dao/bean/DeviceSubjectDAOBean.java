/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.DeviceSubjectDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.device.DeviceSubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class DeviceSubjectDAOBean implements DeviceSubjectDAO {

    private static final Log                   LOG = LogFactory.getLog(DeviceSubjectDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                      entityManager;

    private DeviceSubjectEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                DeviceSubjectEntity.QueryInterface.class);
    }

    public DeviceSubjectEntity findSubject(String userId) {

        LOG.debug("find device subject: " + userId);
        DeviceSubjectEntity subject = this.entityManager.find(DeviceSubjectEntity.class, userId);
        return subject;
    }

    public DeviceSubjectEntity addSubject(String userId) {

        LOG.debug("add device subject: " + userId);
        DeviceSubjectEntity subject = new DeviceSubjectEntity(userId);
        this.entityManager.persist(subject);
        return subject;
    }

    public DeviceSubjectEntity getSubject(String userId) throws SubjectNotFoundException {

        LOG.debug("get device subject: " + userId);
        DeviceSubjectEntity subject = findSubject(userId);
        if (null == subject)
            throw new SubjectNotFoundException();
        return subject;
    }

    public void removeSubject(DeviceSubjectEntity subject) {

        this.entityManager.remove(subject);
    }

    public DeviceSubjectEntity getSubject(SubjectEntity deviceRegistration) throws SubjectNotFoundException {

        DeviceSubjectEntity deviceSubject = this.queryObject.findSubject(deviceRegistration);
        if (null == deviceSubject) {
            throw new SubjectNotFoundException();
        }
        return deviceSubject;
    }
}
