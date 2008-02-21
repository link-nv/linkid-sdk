/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.RegisteredDeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.model.IdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class RegisteredDeviceDAOBean implements RegisteredDeviceDAO {

	private static final Log LOG = LogFactory
			.getLog(RegisteredDeviceDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@EJB
	private IdGenerator idGenerator;

	private RegisteredDeviceEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						RegisteredDeviceEntity.QueryInterface.class);
	}

	public RegisteredDeviceEntity addRegisteredDevice(SubjectEntity subject,
			DeviceEntity device) {

		String uuid = this.idGenerator.generateId();
		LOG.debug("add registered device: subject=" + subject.getUserId()
				+ " uuid=" + uuid + " device=" + device.getName());
		RegisteredDeviceEntity registeredDevice = new RegisteredDeviceEntity(
				subject, uuid, device);
		this.entityManager.persist(registeredDevice);
		return registeredDevice;
	}

	public RegisteredDeviceEntity findRegisteredDevice(SubjectEntity subject,
			DeviceEntity device) {

		return this.queryObject.getRegisteredDevice(subject, device);
	}

	public List<RegisteredDeviceEntity> listRegisteredDevices(
			SubjectEntity subject) {

		return this.queryObject.listRegisteredDevices(subject);
	}

	public RegisteredDeviceEntity findRegisteredDevice(String id) {
		return this.entityManager.find(RegisteredDeviceEntity.class, id);
	}
}
