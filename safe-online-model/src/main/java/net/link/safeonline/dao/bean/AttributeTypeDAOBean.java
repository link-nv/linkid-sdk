/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
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

	public void addAttributeType(AttributeTypeEntity attributeType) {
		LOG.debug("add attribute type: " + attributeType.getName());
		this.entityManager.persist(attributeType);
	}

	public AttributeTypeEntity findAttributeType(String name) {
		LOG.debug("find attribute type: " + name);
		AttributeTypeEntity attributeType = this.entityManager.find(
				AttributeTypeEntity.class, name);
		return attributeType;
	}

	@SuppressWarnings("unchecked")
	public List<AttributeTypeEntity> getAttributeTypes() {
		LOG.debug("get attribute types");
		Query query = AttributeTypeEntity.createQueryAll(this.entityManager);
		List<AttributeTypeEntity> attributeTypes = query.getResultList();
		return attributeTypes;
	}

	public AttributeTypeEntity getAttributeType(String name)
			throws AttributeTypeNotFoundException {
		LOG.debug("get attribute type: " + name);
		AttributeTypeEntity attributeType = findAttributeType(name);
		if (null == attributeType) {
			throw new AttributeTypeNotFoundException();
		}
		return attributeType;
	}
}
