/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceDescriptionPK;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.DevicePropertyPK;
import net.link.safeonline.entity.DeviceType;
import net.link.safeonline.jpa.QueryObjectFactory;

@Stateless
public class DeviceDAOBean implements DeviceDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private DeviceEntity.QueryInterface queryObject;

	private DeviceDescriptionEntity.QueryInterface descriptionQueryObject;

	private DevicePropertyEntity.QueryInterface propertyQueryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, DeviceEntity.QueryInterface.class);
		this.descriptionQueryObject = QueryObjectFactory.createQueryObject(
				this.entityManager,
				DeviceDescriptionEntity.QueryInterface.class);
		this.propertyQueryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, DevicePropertyEntity.QueryInterface.class);
	}

	public DeviceEntity addDevice(String name, DeviceType deviceType) {
		DeviceEntity device = new DeviceEntity(name, deviceType);
		this.entityManager.persist(device);
		return device;
	}

	public DeviceEntity addDevice(String name, DeviceClassEntity deviceClass,
			String authenticationURL, String registrationURL,
			String removalURL, X509Certificate certificate) {
		DeviceEntity device = new DeviceEntity(name, deviceClass,
				authenticationURL, registrationURL, removalURL, certificate);
		this.entityManager.persist(device);
		return device;
	}

	public List<DeviceEntity> listDevices() {
		List<DeviceEntity> result = this.queryObject.listDevices();
		return result;
	}

	public List<DeviceEntity> listDevices(DeviceClassEntity deviceClass) {
		return this.queryObject.listDevices(deviceClass);
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

	public void removeDevice(String deviceName) {
		DeviceEntity device = findDevice(deviceName);
		this.entityManager.remove(device);
	}

	public List<DeviceDescriptionEntity> listDescriptions(DeviceEntity device) {
		return this.descriptionQueryObject.listDescriptions(device);
	}

	public List<DevicePropertyEntity> listProperties(DeviceEntity device) {
		return this.propertyQueryObject.listProperties(device);
	}

	public void addDescription(DeviceEntity device,
			DeviceDescriptionEntity description) {
		/*
		 * Manage relationships.
		 */
		description.setDevice(device);
		device.getDescriptions().put(description.getLanguage(), description);
		/*
		 * Persist.
		 */
		this.entityManager.persist(description);
	}

	public void removeDescription(DeviceDescriptionEntity description) {
		/*
		 * Manage relationships.
		 */
		String language = description.getLanguage();
		description.getDevice().getDescriptions().remove(language);
		/*
		 * Remove from database.
		 */
		this.entityManager.remove(description);
	}

	public void saveDescription(DeviceDescriptionEntity description) {
		this.entityManager.merge(description);
	}

	public DeviceDescriptionEntity getDescription(
			DeviceDescriptionPK descriptionPK)
			throws DeviceDescriptionNotFoundException {
		DeviceDescriptionEntity description = this.entityManager.find(
				DeviceDescriptionEntity.class, descriptionPK);
		if (null == description) {
			throw new DeviceDescriptionNotFoundException();
		}
		return description;
	}

	public DeviceDescriptionEntity findDescription(
			DeviceDescriptionPK descriptionPK) {
		DeviceDescriptionEntity description = this.entityManager.find(
				DeviceDescriptionEntity.class, descriptionPK);
		return description;
	}

	public void addProperty(DeviceEntity device, DevicePropertyEntity property) {
		/*
		 * Manage relationships.
		 */
		property.setDevice(device);
		device.getProperties().put(property.getName(), property);
		/*
		 * Persist.
		 */
		this.entityManager.persist(property);
	}

	public void removeProperty(DevicePropertyEntity property) {
		/*
		 * Manage relationships.
		 */
		String name = property.getName();
		property.getDevice().getProperties().remove(name);
		/*
		 * Remove from database.
		 */
		this.entityManager.remove(property);
	}

	public void saveProperty(DevicePropertyEntity property) {
		this.entityManager.merge(property);
	}

	public DevicePropertyEntity getProperty(DevicePropertyPK propertyPK)
			throws DevicePropertyNotFoundException {
		DevicePropertyEntity property = this.entityManager.find(
				DevicePropertyEntity.class, propertyPK);
		if (null == property) {
			throw new DevicePropertyNotFoundException();
		}
		return property;
	}

	public DevicePropertyEntity findProperty(DevicePropertyPK propertyPK) {
		DevicePropertyEntity property = this.entityManager.find(
				DevicePropertyEntity.class, propertyPK);
		return property;
	}

}
