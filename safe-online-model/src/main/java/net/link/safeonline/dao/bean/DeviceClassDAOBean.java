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
import net.link.safeonline.authentication.exception.DeviceClassDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.dao.DeviceClassDAO;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassDescriptionPK;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = DeviceClassDAO.JNDI_BINDING)
public class DeviceClassDAOBean implements DeviceClassDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                               entityManager;

    private DeviceClassEntity.QueryInterface            queryObject;

    private DeviceClassDescriptionEntity.QueryInterface descriptionQueryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, DeviceClassEntity.QueryInterface.class);
        descriptionQueryObject = QueryObjectFactory.createQueryObject(entityManager,
                DeviceClassDescriptionEntity.QueryInterface.class);
    }

    public DeviceClassEntity addDeviceClass(String name, String authenticationContextClass) {

        DeviceClassEntity deviceClass = new DeviceClassEntity(name, authenticationContextClass);
        entityManager.persist(deviceClass);
        return deviceClass;
    }

    public void removeDeviceClass(String name) {

        DeviceClassEntity deviceClass = findDeviceClass(name);
        entityManager.remove(deviceClass);
    }

    public DeviceClassEntity findDeviceClass(String deviceClassName) {

        return entityManager.find(DeviceClassEntity.class, deviceClassName);
    }

    public DeviceClassEntity getDeviceClass(String deviceClassName)
            throws DeviceClassNotFoundException {

        DeviceClassEntity deviceClass = entityManager.find(DeviceClassEntity.class, deviceClassName);
        if (null == deviceClass)
            throw new DeviceClassNotFoundException();
        return deviceClass;
    }

    public List<DeviceClassEntity> listDeviceClasses() {

        return queryObject.listDeviceClasses();
    }

    public List<DeviceClassDescriptionEntity> listDescriptions(DeviceClassEntity deviceClass) {

        return descriptionQueryObject.listDescriptions(deviceClass);
    }

    public void addDescription(DeviceClassEntity deviceClass, DeviceClassDescriptionEntity description) {

        /*
         * Manage relationships.
         */
        description.setDeviceClass(deviceClass);
        deviceClass.getDescriptions().put(description.getLanguage(), description);
        /*
         * Persist.
         */
        entityManager.persist(description);
    }

    public void removeDescription(DeviceClassDescriptionEntity description) {

        /*
         * Manage relationships.
         */
        String language = description.getLanguage();
        description.getDeviceClass().getDescriptions().remove(language);
        /*
         * Remove from database.
         */
        entityManager.remove(description);
    }

    public void saveDescription(DeviceClassDescriptionEntity description) {

        entityManager.merge(description);
    }

    public DeviceClassDescriptionEntity getDescription(DeviceClassDescriptionPK descriptionPK)
            throws DeviceClassDescriptionNotFoundException {

        DeviceClassDescriptionEntity description = entityManager.find(DeviceClassDescriptionEntity.class, descriptionPK);
        if (null == description)
            throw new DeviceClassDescriptionNotFoundException();
        return description;
    }

    public DeviceClassDescriptionEntity findDescription(DeviceClassDescriptionPK descriptionPK) {

        DeviceClassDescriptionEntity description = entityManager.find(DeviceClassDescriptionEntity.class, descriptionPK);
        return description;
    }

}
