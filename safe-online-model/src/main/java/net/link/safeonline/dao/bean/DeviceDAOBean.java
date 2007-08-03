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
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

@Stateless
public class DeviceDAOBean implements DeviceDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private DeviceEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, DeviceEntity.QueryInterface.class);
	}

	public DeviceEntity addDevice(String name) {
		DeviceEntity device = new DeviceEntity(name);
		this.entityManager.persist(device);
		return device;
	}

	public List<DeviceEntity> listDevices() {
		List<DeviceEntity> result = this.queryObject.listDevices();
		return result;
	}

	public DeviceEntity findDevice(String deviceName) {
		return this.entityManager.find(DeviceEntity.class, deviceName);
	}

	public DeviceEntity getDevice(String name) throws DeviceNotFoundException {
		DeviceEntity device = this.entityManager.find(DeviceEntity.class, name);
		if (null == device) {
			throw new DeviceNotFoundException();
		}
		return device;
	}
}
