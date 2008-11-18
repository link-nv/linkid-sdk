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
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.dao.NodeMappingDAO;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.model.IdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = NodeMappingDAO.JNDI_BINDING)
public class NodeMappingDAOBean implements NodeMappingDAO {

    private static final Log                 LOG = LogFactory.getLog(NodeMappingDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                    entityManager;

    @EJB(mappedName = IdGenerator.JNDI_BINDING)
    private IdGenerator                      idGenerator;

    private NodeMappingEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, NodeMappingEntity.QueryInterface.class);
    }

    public NodeMappingEntity addNodeMapping(SubjectEntity subject, NodeEntity node) {

        String uuid = this.idGenerator.generateId();
        LOG.debug("add node mapping: subject=" + subject.getUserId() + " uuid=" + uuid + " node=" + node.getName());
        NodeMappingEntity registeredNode = new NodeMappingEntity(subject, uuid, node);
        this.entityManager.persist(registeredNode);
        return registeredNode;
    }

    public NodeMappingEntity findNodeMapping(SubjectEntity subject, NodeEntity node) {

        return this.queryObject.findNodeMapping(subject, node);
    }

    public List<NodeMappingEntity> listNodeMappings(SubjectEntity subject) {

        return this.queryObject.listNodeMappings(subject);
    }

    public NodeMappingEntity findNodeMapping(String id) {

        return this.entityManager.find(NodeMappingEntity.class, id);
    }

    public NodeMappingEntity getNodeMapping(String id)
            throws NodeMappingNotFoundException {

        NodeMappingEntity nodeMapping = this.findNodeMapping(id);
        if (null == nodeMapping)
            throw new NodeMappingNotFoundException();
        return nodeMapping;
    }

    public void removeNodeMappings(SubjectEntity subject) {

        this.queryObject.deleteAll(subject);
    }

    public List<NodeMappingEntity> listNodeMappings(NodeEntity node) {

        return this.queryObject.listNodeMappings(node);
    }
}
