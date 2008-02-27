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
	private EntityManager entityManager;

	private OlasEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, OlasEntity.QueryInterface.class);
	}

	public OlasEntity addNode(String name, String location,
			X509Certificate certificate) {
		OlasEntity olas = new OlasEntity(name, location, certificate);
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

	public void removeNode(OlasEntity node) {
		this.entityManager.remove(node);
	}
}
