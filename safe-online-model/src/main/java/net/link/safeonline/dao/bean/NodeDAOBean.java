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
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.dao.NodeDAO;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = NodeDAO.JNDI_BINDING)
public class NodeDAOBean implements NodeDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager             entityManager;

    private NodeEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, NodeEntity.QueryInterface.class);
    }

    public NodeEntity addNode(String name, String protocol, String hostname, int port, int sslPort, X509Certificate certificate) {

        NodeEntity node = new NodeEntity(name, protocol, hostname, port, sslPort, certificate);
        entityManager.persist(node);
        return node;
    }

    public List<NodeEntity> listNodes() {

        List<NodeEntity> result = queryObject.listNodeEntities();
        return result;
    }

    public NodeEntity findNode(String name) {

        NodeEntity node = entityManager.find(NodeEntity.class, name);
        return node;
    }

    public NodeEntity getNode(String name)
            throws NodeNotFoundException {

        NodeEntity node = findNode(name);
        if (null == node)
            throw new NodeNotFoundException();
        return node;
    }

    public NodeEntity getNodeFromCertificate(X509Certificate certificate)
            throws NodeNotFoundException {

        NodeEntity node = findNodeFromCertificate(certificate);
        if (node == null)
            throw new NodeNotFoundException();

        return node;
    }

    public NodeEntity findNodeFromCertificate(X509Certificate certificate) {

        List<NodeEntity> nodes = queryObject.listNodeEntitiesWhereCertificateSubject(certificate.getSubjectX500Principal().getName());
        if (nodes.isEmpty())
            return null;
        NodeEntity node = nodes.get(0);
        return node;
    }

    public void removeNode(NodeEntity node) {

        entityManager.remove(node);
    }
}
