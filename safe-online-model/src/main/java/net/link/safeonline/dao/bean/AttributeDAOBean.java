/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class AttributeDAOBean implements AttributeDAO {

	private static final Log LOG = LogFactory.getLog(AttributeDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public void addAttribute(String attributeTypeName, String subjectLogin,
			String stringValue) {
		LOG.debug("add attribute " + attributeTypeName + " for subject "
				+ subjectLogin + " with value: " + stringValue);
		AttributeEntity attribute = new AttributeEntity(attributeTypeName,
				subjectLogin, stringValue);
		this.entityManager.persist(attribute);
	}

	public AttributeEntity findAttribute(String attributeTypeName,
			String subjectLogin) {
		LOG.debug("find attribute for type  " + attributeTypeName
				+ " and subject " + subjectLogin);
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, new AttributePK(attributeTypeName,
						subjectLogin));
		return attribute;
	}

	@SuppressWarnings("unchecked")
	public List<AttributeEntity> getAttributes(SubjectEntity subject) {
		LOG.debug("get attributes for subject " + subject.getLogin());
		Query query = AttributeEntity.createQueryWhereSubject(
				this.entityManager, subject);
		List<AttributeEntity> attributes = query.getResultList();
		return attributes;
	}

	public void addAttribute(AttributeTypeEntity attributeType,
			String subjectLogin, String stringValue) {
		LOG.debug("add attribute " + attributeType.getName());
		AttributeEntity attribute = new AttributeEntity(
				attributeType.getName(), subjectLogin, stringValue);
		attribute.setAttributeType(attributeType);
		this.entityManager.persist(attribute);
	}
}
