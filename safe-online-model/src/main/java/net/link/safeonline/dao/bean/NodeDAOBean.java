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


@Stateless
@LocalBinding(jndiBinding = NodeDAO.JNDI_BINDING)
public class NodeDAOBean implements NodeDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager             entityManager;

    private NodeEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, NodeEntity.QueryInterface.class);
    }

    public NodeEntity addNode(String name, String protocol, String hostname, int port, int sslPort, X509Certificate authnCertificate,
                              X509Certificate signingCertificate) {

        NodeEntity node = new NodeEntity(name, protocol, hostname, port, sslPort, authnCertificate, signingCertificate);
        this.entityManager.persist(node);
        return node;
    }

    public List<NodeEntity> listNodes() {

        List<NodeEntity> result = this.queryObject.listNodeEntities();
        return result;
    }

    public NodeEntity findNode(String name) {

        NodeEntity node = this.entityManager.find(NodeEntity.class, name);
        return node;
    }

    public NodeEntity getNode(String name) throws NodeNotFoundException {

        NodeEntity node = findNode(name);
        if (null == node) {
            throw new NodeNotFoundException();
        }
        return node;
    }

    public NodeEntity getNodeFromAuthnCertificate(X509Certificate authnCertificate) throws NodeNotFoundException {

        List<NodeEntity> nodes = this.queryObject.listNodeEntitiesWhereAuthnCertificateSubject(authnCertificate.getSubjectX500Principal()
                                                                                                               .getName());
        if (nodes.isEmpty()) {
            throw new NodeNotFoundException();
        }
        NodeEntity node = nodes.get(0);
        return node;
    }

    public NodeEntity findNodeFromAuthnCertificate(X509Certificate authnCertificate) {

        List<NodeEntity> nodes = this.queryObject.listNodeEntitiesWhereAuthnCertificateSubject(authnCertificate.getSubjectX500Principal()
                                                                                                               .getName());
        if (nodes.isEmpty())
            return null;
        NodeEntity node = nodes.get(0);
        return node;
    }

    public NodeEntity getNodeFromSigningCertificate(X509Certificate signingCertificate) throws NodeNotFoundException {

        List<NodeEntity> nodes = this.queryObject
                                                 .listNodeEntitiesWhereSigningCertificateSubject(signingCertificate
                                                                                                                   .getSubjectX500Principal()
                                                                                                                   .getName());
        if (nodes.isEmpty()) {
            throw new NodeNotFoundException();
        }
        NodeEntity node = nodes.get(0);
        return node;
    }

    public NodeEntity findNodeFromSigningCertificate(X509Certificate signingCertificate) {

        List<NodeEntity> nodes = this.queryObject
                                                 .listNodeEntitiesWhereSigningCertificateSubject(signingCertificate
                                                                                                                   .getSubjectX500Principal()
                                                                                                                   .getName());
        if (nodes.isEmpty())
            return null;
        NodeEntity node = nodes.get(0);
        return node;
    }

    public void removeNode(NodeEntity node) {

        this.entityManager.remove(node);
    }
}
