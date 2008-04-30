/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;

@Local
public interface EndpointReferenceDAO {

	EndpointReferenceEntity addEndpointReference(String address,
			ApplicationEntity application);

	EndpointReferenceEntity addEndpointReference(String address,
			DeviceEntity device);

	EndpointReferenceEntity findEndpointReference(String address,
			ApplicationEntity application);

	EndpointReferenceEntity findEndpointReference(String address,
			DeviceEntity device);

	EndpointReferenceEntity getEndpointReference(String address,
			ApplicationEntity application)
			throws EndpointReferenceNotFoundException;

	EndpointReferenceEntity getEndpointReference(String address,
			DeviceEntity device) throws EndpointReferenceNotFoundException;

	List<EndpointReferenceEntity> listEndpoints();

	List<EndpointReferenceEntity> listEndpoints(DeviceEntity device);

	List<EndpointReferenceEntity> listEndpoints(ApplicationEntity application);

	void remove(EndpointReferenceEntity endpoint);

}
