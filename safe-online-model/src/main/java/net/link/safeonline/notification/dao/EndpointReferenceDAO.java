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
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;


@Local
public interface EndpointReferenceDAO {

    EndpointReferenceEntity addEndpointReference(String address, ApplicationEntity application);

    EndpointReferenceEntity addEndpointReference(String address, NodeEntity node);

    EndpointReferenceEntity findEndpointReference(String address, ApplicationEntity application);

    EndpointReferenceEntity findEndpointReference(String address, NodeEntity node);
    
    EndpointReferenceEntity findEndpointReference(long id);

    EndpointReferenceEntity getEndpointReference(String address, ApplicationEntity application)
            throws EndpointReferenceNotFoundException;

    EndpointReferenceEntity getEndpointReference(String address, NodeEntity node)
            throws EndpointReferenceNotFoundException;

    List<EndpointReferenceEntity> listEndpoints();

    List<EndpointReferenceEntity> listEndpoints(NodeEntity node);

    List<EndpointReferenceEntity> listEndpoints(ApplicationEntity application);

    void remove(EndpointReferenceEntity endpoint);

}
