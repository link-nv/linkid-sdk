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
import net.link.safeonline.dao.RegisteredDeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

@Stateless
public class RegisteredDeviceDAOBean implements RegisteredDeviceDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private RegisteredDeviceEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						RegisteredDeviceEntity.QueryInterface.class);
	}

	public RegisteredDeviceEntity addRegisteredDevice(SubjectEntity subject,
			String id, String humanReadableId, DeviceEntity device) {
		RegisteredDeviceEntity registeredDevice = new RegisteredDeviceEntity(
				subject, id, humanReadableId, device);
		this.entityManager.persist(registeredDevice);
		return registeredDevice;
	}

	public List<RegisteredDeviceEntity> listRegisteredDevices(
			SubjectEntity subject) {
		return this.queryObject.listRegisteredDevices(subject);
	}

}
