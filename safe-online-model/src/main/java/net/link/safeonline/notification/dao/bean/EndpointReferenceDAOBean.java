/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.notification.dao.EndpointReferenceDAO;
import net.link.safeonline.notification.dao.NotificationProducerDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = EndpointReferenceDAO.JNDI_BINDING)
public class EndpointReferenceDAOBean implements EndpointReferenceDAO {

    private static final Log               LOG = LogFactory.getLog(EndpointReferenceDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                  entityManager;

    EndpointReferenceEntity.QueryInterface queryObject;

    @EJB(mappedName = NotificationProducerDAO.JNDI_BINDING)
    private NotificationProducerDAO        notificationProducerDAO;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, EndpointReferenceEntity.QueryInterface.class);
    }

    public EndpointReferenceEntity addEndpointReference(String address, ApplicationEntity application) {

        LOG.debug("add endpoint: " + address + ", " + application.getName());
        EndpointReferenceEntity endpointReference = new EndpointReferenceEntity(address, application);
        this.entityManager.persist(endpointReference);
        return endpointReference;
    }

    public EndpointReferenceEntity addEndpointReference(String address, NodeEntity node) {

        LOG.debug("add endpoint: " + address + ", " + node.getName());
        EndpointReferenceEntity endpointReference = new EndpointReferenceEntity(address, node);
        this.entityManager.persist(endpointReference);
        return endpointReference;
    }

    public EndpointReferenceEntity findEndpointReference(String address, ApplicationEntity application) {

        LOG.debug("find endpoint ref: address=" + address + " application=" + application.getName());
        return this.queryObject.find(address, application);
    }

    public EndpointReferenceEntity findEndpointReference(String address, NodeEntity node) {

        LOG.debug("find endpoint ref: address=" + address + " node=" + node.getName());
        return this.queryObject.find(address, node);
    }

    public EndpointReferenceEntity findEndpointReference(long id) {

        LOG.debug("find endpoint ref: id=" + id);
        return this.entityManager.find(EndpointReferenceEntity.class, id);
    }

    public EndpointReferenceEntity getEndpointReference(String address, ApplicationEntity application)
            throws EndpointReferenceNotFoundException {

        EndpointReferenceEntity endpointReference = findEndpointReference(address, application);
        if (null == endpointReference)
            throw new EndpointReferenceNotFoundException();
        return endpointReference;
    }

    public EndpointReferenceEntity getEndpointReference(String address, NodeEntity node)
            throws EndpointReferenceNotFoundException {

        EndpointReferenceEntity endpointReference = findEndpointReference(address, node);
        if (null == endpointReference)
            throw new EndpointReferenceNotFoundException();
        return endpointReference;
    }

    public List<EndpointReferenceEntity> listEndpoints() {

        return this.queryObject.listEndpoints();
    }

    public List<EndpointReferenceEntity> listEndpoints(NodeEntity node) {

        return this.queryObject.listEndpoints(node);
    }

    public List<EndpointReferenceEntity> listEndpoints(ApplicationEntity application) {

        return this.queryObject.listEndpoints(application);
    }

    public void remove(EndpointReferenceEntity endpoint) {

        LOG.debug("remove endpoint: " + endpoint.getAddress() + ", " + endpoint.getName());
        // Manage relationships.
        List<NotificationProducerSubscriptionEntity> topics = this.notificationProducerDAO.listTopics();
        for (NotificationProducerSubscriptionEntity topic : topics) {
            topic.getConsumers().remove(endpoint);
        }

        // Remove from database.
        this.entityManager.remove(endpoint);
    }
}
