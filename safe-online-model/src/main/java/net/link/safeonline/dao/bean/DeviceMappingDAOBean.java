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
import net.link.safeonline.dao.DeviceMappingDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.model.IdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class DeviceMappingDAOBean implements DeviceMappingDAO {

	private static final Log LOG = LogFactory
			.getLog(DeviceMappingDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	@EJB
	private IdGenerator idGenerator;

	private DeviceMappingEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, DeviceMappingEntity.QueryInterface.class);
	}

	public DeviceMappingEntity addDeviceMapping(SubjectEntity subject,
			DeviceEntity device) {

		String uuid = this.idGenerator.generateId();
		LOG.debug("add device mapping: subject=" + subject.getUserId()
				+ " uuid=" + uuid + " device=" + device.getName());
		DeviceMappingEntity registeredDevice = new DeviceMappingEntity(subject,
				uuid, device);
		this.entityManager.persist(registeredDevice);
		return registeredDevice;
	}

	public DeviceMappingEntity findDeviceMapping(SubjectEntity subject,
			DeviceEntity device) {

		return this.queryObject.getDeviceMapping(subject, device);
	}

	public List<DeviceMappingEntity> listDeviceMappings(SubjectEntity subject) {

		return this.queryObject.listDeviceMappings(subject);
	}

	public DeviceMappingEntity findDeviceMapping(String id) {
		return this.entityManager.find(DeviceMappingEntity.class, id);
	}
}
