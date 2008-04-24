/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.dao.bean;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.notification.dao.EndpointReferenceDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class EndpointReferenceDAOBean implements EndpointReferenceDAO {

	private static final Log LOG = LogFactory
			.getLog(EndpointReferenceDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	EndpointReferenceEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager,
				EndpointReferenceEntity.QueryInterface.class);
	}

	public EndpointReferenceEntity addEndpointReference(String address,
			ApplicationEntity application) {
		LOG.debug("add endpoint: " + address + ", " + application.getName());
		EndpointReferenceEntity endpointReference = new EndpointReferenceEntity(
				address, application);
		this.entityManager.persist(endpointReference);
		return endpointReference;
	}

	public EndpointReferenceEntity addEndpointReference(String address,
			DeviceEntity device) {
		LOG.debug("add endpoint: " + address + ", " + device.getName());
		EndpointReferenceEntity endpointReference = new EndpointReferenceEntity(
				address, device);
		this.entityManager.persist(endpointReference);
		return endpointReference;
	}

	public EndpointReferenceEntity findEndpointReference(String address,
			ApplicationEntity application) {
		return this.queryObject.find(address, application);
	}

	public EndpointReferenceEntity findEndpointReference(String address,
			DeviceEntity device) {
		return this.queryObject.find(address, device);
	}

	public EndpointReferenceEntity getEndpointReference(String address,
			ApplicationEntity application)
			throws EndpointReferenceNotFoundException {
		EndpointReferenceEntity endpointReference = findEndpointReference(
				address, application);
		if (null == endpointReference)
			throw new EndpointReferenceNotFoundException();
		return endpointReference;
	}

	public EndpointReferenceEntity getEndpointReference(String address,
			DeviceEntity device) throws EndpointReferenceNotFoundException {
		EndpointReferenceEntity endpointReference = findEndpointReference(
				address, device);
		if (null == endpointReference)
			throw new EndpointReferenceNotFoundException();
		return endpointReference;
	}
}
