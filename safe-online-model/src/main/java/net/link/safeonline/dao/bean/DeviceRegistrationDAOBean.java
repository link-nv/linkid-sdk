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
import net.link.safeonline.dao.DeviceRegistrationDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.model.IdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class DeviceRegistrationDAOBean implements DeviceRegistrationDAO {

	private static final Log LOG = LogFactory
			.getLog(DeviceRegistrationDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@EJB
	private IdGenerator idGenerator;

	private DeviceRegistrationEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						DeviceRegistrationEntity.QueryInterface.class);
	}

	public DeviceRegistrationEntity addRegisteredDevice(SubjectEntity subject,
			DeviceEntity device) {

		String uuid = this.idGenerator.generateId();
		LOG.debug("add registered device: subject=" + subject.getUserId()
				+ " uuid=" + uuid + " device=" + device.getName());
		DeviceRegistrationEntity registeredDevice = new DeviceRegistrationEntity(
				subject, uuid, device);
		this.entityManager.persist(registeredDevice);
		return registeredDevice;
	}

	public DeviceRegistrationEntity findRegisteredDevice(SubjectEntity subject,
			DeviceEntity device) {

		return this.queryObject.getRegisteredDevice(subject, device);
	}

	public List<DeviceRegistrationEntity> listRegisteredDevices(
			SubjectEntity subject) {

		return this.queryObject.listRegisteredDevices(subject);
	}

	public DeviceRegistrationEntity findRegisteredDevice(String id) {
		return this.entityManager.find(DeviceRegistrationEntity.class, id);
	}
}
