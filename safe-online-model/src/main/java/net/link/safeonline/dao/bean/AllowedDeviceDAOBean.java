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
import net.link.safeonline.dao.AllowedDeviceDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.jpa.QueryObjectFactory;


@Stateless
public class AllowedDeviceDAOBean implements AllowedDeviceDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager              entityManager;

    AllowedDeviceEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                AllowedDeviceEntity.QueryInterface.class);
    }

    public AllowedDeviceEntity addAllowedDevice(ApplicationEntity application, DeviceEntity device, int weight) {

        AllowedDeviceEntity allowedDevice = new AllowedDeviceEntity(application, device, weight);
        this.entityManager.persist(allowedDevice);
        return allowedDevice;
    }

    public List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application) {

        List<AllowedDeviceEntity> result = this.queryObject.listAllowedDevices(application);
        return result;
    }

    public void deleteAllowedDevices(ApplicationEntity application) {

        this.queryObject.deleteAllowedDevices(application);
    }

    public AllowedDeviceEntity findAllowedDevice(ApplicationEntity application, DeviceEntity device) {

        AllowedDeviceEntity allowedDevice = this.queryObject.find(application, device);
        return allowedDevice;
    }
}
