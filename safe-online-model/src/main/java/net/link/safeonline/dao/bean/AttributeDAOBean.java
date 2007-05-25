/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.EJBException;
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

	public AttributeEntity addAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, String stringValue) {
		LOG.debug("add attribute " + attributeType + " for subject " + subject
				+ " with value: " + stringValue);
		AttributeEntity attribute = new AttributeEntity(attributeType, subject,
				stringValue);
		this.entityManager.persist(attribute);
		return attribute;
	}

	public AttributeEntity addAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index) {
		LOG.debug("add attribute " + attributeType + " for subject " + subject);
		AttributeEntity attribute = new AttributeEntity(attributeType, subject,
				index);
		this.entityManager.persist(attribute);
		return attribute;
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

	private AttributeEntity findAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index) {
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, new AttributePK(attributeType, subject,
						index));
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
			SubjectEntity subject, long index, String stringValue) {
		AttributeEntity attribute = findAttribute(attributeType, subject, index);
		if (null == attribute) {
			attribute = addAttribute(attributeType, subject, index);
		}
		attribute.setStringValue(stringValue);
	}

	public void addOrUpdateAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index, Boolean booleanValue) {
		AttributeEntity attribute = findAttribute(attributeType, subject, index);
		if (null == attribute) {
			attribute = addAttribute(attributeType, subject, index);
		}
		attribute.setBooleanValue(booleanValue);
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

	public AttributeEntity findAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject) {
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, new AttributePK(attributeType, subject));
		return attribute;
	}

	public AttributeEntity getAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject) throws AttributeNotFoundException {
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, new AttributePK(attributeType, subject));
		if (null == attribute) {
			throw new AttributeNotFoundException();
		}
		return attribute;
	}

	@SuppressWarnings("unchecked")
	public List<AttributeEntity> listVisibleAttributes(SubjectEntity subject) {
		Query query = AttributeEntity.createQueryWhereSubjectAndVisible(
				this.entityManager, subject);
		List<AttributeEntity> attributes = query.getResultList();
		return attributes;
	}

	public AttributeEntity getAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index)
			throws AttributeNotFoundException {
		AttributePK pk = new AttributePK(attributeType, subject, index);
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, pk);
		if (null == attribute) {
			throw new AttributeNotFoundException();
		}
		return attribute;
	}

	public void removeAttribute(AttributeEntity attributeEntity) {
		this.entityManager.remove(attributeEntity);
	}

	@SuppressWarnings("unchecked")
	public List<AttributeEntity> listAttributes(SubjectEntity subject,
			AttributeTypeEntity attributeType) {
		Query query = AttributeEntity
				.createQueryWhereSubjectAndAttributeTypeOrdered(
						this.entityManager, subject, attributeType);
		List<AttributeEntity> attributes = query.getResultList();
		return attributes;
	}

	@SuppressWarnings("unchecked")
	public AttributeEntity addAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject) {
		if (false == attributeType.isMultivalued()) {
			throw new EJBException(
					"addAttribute cannot be invoked for single-valued attributes");
		}
		Query query = AttributeEntity.createMaxIdWhereSubjectAndAttributeType(
				this.entityManager, subject, attributeType);
		List<Long> maxId = query.getResultList();
		long index;
		if (maxId.isEmpty()) {
			/*
			 * This means that no other multi-valued attribute of the given
			 * attribute type existed before. This is a weird case to occur, but
			 * we can handle it. This just means we're the first to create it.
			 */
			index = 0;
		} else {
			index = maxId.get(0) + 1;
		}
		AttributeEntity attribute = new AttributeEntity(attributeType, subject,
				index);
		this.entityManager.persist(attribute);
		return attribute;
	}
}
