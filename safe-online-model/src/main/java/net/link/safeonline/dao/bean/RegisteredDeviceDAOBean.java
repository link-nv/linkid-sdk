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

@Stateless
public class RegisteredDeviceDAOBean implements RegisteredDeviceDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@EJB
	private IdGenerator idGenerator;

	private RegisteredDeviceEntity.SubjectRegistrationsQuery subjectRegistrationsQuery;
	private RegisteredDeviceEntity.SubjectDeviceRegistrationsQuery subjectDeviceRegistrationQuery;

	@PostConstruct
	public void postConstructCallback() {
		this.subjectRegistrationsQuery = QueryObjectFactory.createQueryObject(
				this.entityManager,
				RegisteredDeviceEntity.SubjectRegistrationsQuery.class);
		this.subjectDeviceRegistrationQuery = QueryObjectFactory
				.createQueryObject(
						this.entityManager,
						RegisteredDeviceEntity.SubjectDeviceRegistrationsQuery.class);
	}

	public RegisteredDeviceEntity addRegisteredDevice(SubjectEntity subject,
			DeviceEntity device) {

		String uuid = this.idGenerator.generateId();
		RegisteredDeviceEntity registeredDevice = new RegisteredDeviceEntity(
				subject, uuid, device);
		this.entityManager.persist(registeredDevice);
		return registeredDevice;
	}

	public RegisteredDeviceEntity findRegisteredDevice(SubjectEntity subject,
			DeviceEntity device) {

		return this.subjectDeviceRegistrationQuery.getRegisteredDevice(subject,
				device);
	}

	public List<RegisteredDeviceEntity> listRegisteredDevices(
			SubjectEntity subject) {

		return this.subjectRegistrationsQuery.listRegisteredDevices(subject);
	}
}
