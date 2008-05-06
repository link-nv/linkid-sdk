/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class AttributeDAOBean implements AttributeDAO {

	private static final Log LOG = LogFactory.getLog(AttributeDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private AttributeEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, AttributeEntity.QueryInterface.class);
	}

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
			SubjectEntity subject) {
		LOG.debug("find attribute for type  " + attributeTypeName
				+ " and subject " + subject.getUserId());
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, new AttributePK(attributeTypeName,
						subject.getUserId()));
		return attribute;
	}

	public AttributeEntity findAttribute(SubjectEntity subject,
			AttributeTypeEntity attributeType, long index) {
		LOG.debug("find attribute for type  " + attributeType.getName()
				+ " and subject " + subject.getUserId());
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, new AttributePK(attributeType, subject,
						index));
		return attribute;
	}

	public Map<AttributeTypeEntity, List<AttributeEntity>> listAttributes(
			SubjectEntity subject) {
		LOG.debug("get attributes for subject " + subject.getUserId());
		List<AttributeEntity> attributes = this.queryObject
				.listAttributes(subject);
		Map<AttributeTypeEntity, List<AttributeEntity>> result = new HashMap<AttributeTypeEntity, List<AttributeEntity>>();
		for (AttributeEntity attribute : attributes) {
			List<AttributeEntity> list = result.get(attribute
					.getAttributeType());
			if (null == list) {
				list = new LinkedList<AttributeEntity>();
				result.put(attribute.getAttributeType(), list);
			}
			list.add(attribute);
		}
		return result;
	}

	public void addOrUpdateAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index, String stringValue) {
		AttributeEntity attribute = findAttribute(subject, attributeType, index);
		if (null == attribute)
			attribute = addAttribute(attributeType, subject, index);
		attribute.setStringValue(stringValue);
	}

	public void addOrUpdateAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index, Boolean booleanValue) {
		AttributeEntity attribute = findAttribute(subject, attributeType, index);
		if (null == attribute)
			attribute = addAttribute(attributeType, subject, index);
		attribute.setBooleanValue(booleanValue);
	}

	public AttributeEntity getAttribute(String attributeTypeName,
			SubjectEntity subject) throws AttributeNotFoundException {
		AttributeEntity attribute = findAttribute(attributeTypeName, subject);
		if (null == attribute)
			throw new AttributeNotFoundException();
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
		if (null == attribute)
			throw new AttributeNotFoundException();
		return attribute;
	}

	public List<AttributeEntity> listVisibleAttributes(SubjectEntity subject) {
		List<AttributeEntity> attributes = this.queryObject
				.listVisibleAttributes(subject);
		return attributes;
	}

	public AttributeEntity getAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index)
			throws AttributeNotFoundException {
		AttributePK pk = new AttributePK(attributeType, subject, index);
		AttributeEntity attribute = this.entityManager.find(
				AttributeEntity.class, pk);
		if (null == attribute)
			throw new AttributeNotFoundException();
		return attribute;
	}

	public void removeAttribute(AttributeEntity attributeEntity) {
		this.entityManager.remove(attributeEntity);
	}

	public List<AttributeEntity> listAttributes(SubjectEntity subject,
			AttributeTypeEntity attributeType) {
		LOG.debug("listAttributes for " + subject.getUserId() + " of type "
				+ attributeType.getName());
		List<AttributeEntity> attributes = this.queryObject.listAttributes(
				subject, attributeType);
		return attributes;
	}

	public AttributeEntity addAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject) {
		long index;
		if (false == attributeType.isMultivalued())
			index = 0;
		else
			index = calcIndex(subject, attributeType);

		AttributeEntity attribute = new AttributeEntity(attributeType, subject,
				index);
		this.entityManager.persist(attribute);
		LOG.debug("addAttribute index: " + index);
		return attribute;
	}

	private long calcIndex(SubjectEntity subject,
			AttributeTypeEntity attributeType) {
		List<Long> maxIds = this.queryObject
				.listMaxIdWhereSubjectAndAttributeType(subject, attributeType);
		if (maxIds.isEmpty())
			/*
			 * This means that no other multi-valued attribute of the given
			 * attribute type existed before.
			 */
			return 0;
		Long maxId = maxIds.get(0);
		if (null == maxId)
			/*
			 * This means that no other multi-valued attribute of the given
			 * attribute type existed before.
			 */
			return 0;
		return maxId + 1;
	}

	public void removeAttributes(SubjectEntity subject) {
		this.queryObject.deleteAttributes(subject);
	}

	public void removeAttributes(AttributeTypeEntity attributeType) {
		int count = this.queryObject.deleteAttributes(attributeType);
		LOG.debug("number of removed attributes: " + count);
	}

	public List<AttributeEntity> listAttributes(String prefix,
			AttributeTypeEntity attributeType) {
		LOG.debug("list attributes of type " + attributeType.getName()
				+ " starting with " + prefix);
		return this.queryObject.listAttributes(prefix, attributeType);
	}
}
