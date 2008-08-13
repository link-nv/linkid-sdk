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
import net.link.safeonline.dao.OlasDAO;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.jpa.QueryObjectFactory;


@Stateless
public class OlasDAOBean implements OlasDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager             entityManager;

    private OlasEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, OlasEntity.QueryInterface.class);
    }

    public OlasEntity addNode(String name, String protocol, String hostname, int port, int sslPort,
            X509Certificate authnCertificate, X509Certificate signingCertificate) {

        OlasEntity olas = new OlasEntity(name, protocol, hostname, port, sslPort, authnCertificate, signingCertificate);
        this.entityManager.persist(olas);
        return olas;
    }

    public List<OlasEntity> listNodes() {

        List<OlasEntity> result = this.queryObject.listOlasEntities();
        return result;
    }

    public OlasEntity findNode(String name) {

        OlasEntity node = this.entityManager.find(OlasEntity.class, name);
        return node;
    }

    public OlasEntity getNode(String name) throws NodeNotFoundException {

        OlasEntity node = findNode(name);
        if (null == node)
            throw new NodeNotFoundException();
        return node;
    }

    public OlasEntity getNodeFromAuthnCertificate(X509Certificate authnCertificate) throws NodeNotFoundException {

        List<OlasEntity> nodes = this.queryObject.listOlasEntitiesWhereAuthnCertificateSubject(authnCertificate
                .getSubjectX500Principal().getName());
        if (nodes.isEmpty())
            throw new NodeNotFoundException();
        OlasEntity node = nodes.get(0);
        return node;
    }

    public OlasEntity getNodeFromSigningCertificate(X509Certificate signingCertificate) throws NodeNotFoundException {

        List<OlasEntity> nodes = this.queryObject.listOlasEntitiesWhereSigningCertificateSubject(signingCertificate
                .getSubjectX500Principal().getName());
        if (nodes.isEmpty())
            throw new NodeNotFoundException();
        OlasEntity node = nodes.get(0);
        return node;
    }

    public void removeNode(OlasEntity node) {

        this.entityManager.remove(node);
    }
}
