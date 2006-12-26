/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class AttributeTypeDAOBean implements AttributeTypeDAO {

	private static final Log LOG = LogFactory
			.getLog(AttributeTypeDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public void addAttributeType(String name, String type) {
		LOG.debug("add attribute type: " + name);
		AttributeTypeEntity attributeType = new AttributeTypeEntity(name, type);
		this.entityManager.persist(attributeType);
	}

	public AttributeTypeEntity findAttributeType(String name) {
		LOG.debug("find attribute type: " + name);
		AttributeTypeEntity attributeType = this.entityManager.find(
				AttributeTypeEntity.class, name);
		return attributeType;
	}
}
