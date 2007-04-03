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
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
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

	public void addAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, String stringValue) {
		LOG.debug("add attribute " + attributeType + " for subject " + subject
				+ " with value: " + stringValue);
		AttributeEntity attribute = new AttributeEntity(attributeType, subject,
				stringValue);
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
	public List<AttributeEntity> listAttributes(SubjectEntity subject) {
		LOG.debug("get attributes for subject " + subject.getLogin());
		Query query = AttributeEntity.createQueryWhereSubject(
				this.entityManager, subject);
		List<AttributeEntity> attributes = query.getResultList();
		return attributes;
	}

	public void addOrUpdateAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, String stringValue) {
		AttributeEntity attribute = findAttribute(attributeType.getName(),
				subject.getLogin());
		if (null != attribute) {
			attribute.setStringValue(stringValue);
			return;
		}
		addAttribute(attributeType, subject, stringValue);
	}

	public AttributeEntity getAttribute(String attributeTypeName,
			String subjectLogin) throws AttributeNotFoundException {
		AttributeEntity attribute = findAttribute(attributeTypeName,
				subjectLogin);
		if (null == attribute) {
			throw new AttributeNotFoundException();
		}
		return attribute;
	}
}
