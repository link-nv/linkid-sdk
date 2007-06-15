/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;

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
	public List<AttributeTypeEntity> listAttributeTypes() {
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

	@SuppressWarnings("unchecked")
	public List<AttributeTypeDescriptionEntity> listDescriptions(
			AttributeTypeEntity attributeType) {
		Query query = AttributeTypeDescriptionEntity
				.createQueryWhereAttributeType(this.entityManager,
						attributeType);
		List<AttributeTypeDescriptionEntity> descriptions = query
				.getResultList();
		return descriptions;
	}

	public void addAttributeTypeDescription(AttributeTypeEntity attributeType,
			AttributeTypeDescriptionEntity newAttributeTypeDescription) {
		/*
		 * Manage relationships.
		 */
		newAttributeTypeDescription.setAttributeType(attributeType);
		attributeType.getDescriptions().put(
				newAttributeTypeDescription.getLanguage(),
				newAttributeTypeDescription);
		/*
		 * Persist.
		 */
		this.entityManager.persist(newAttributeTypeDescription);
	}

	public void removeDescription(
			AttributeTypeDescriptionEntity attributeTypeDescription) {
		/*
		 * Manage relationships.
		 */
		String language = attributeTypeDescription.getLanguage();
		attributeTypeDescription.getAttributeType().getDescriptions().remove(
				language);
		/*
		 * Remove from database.
		 */
		this.entityManager.remove(attributeTypeDescription);
	}

	public void saveDescription(
			AttributeTypeDescriptionEntity attributeTypeDescription) {
		this.entityManager.merge(attributeTypeDescription);
	}

	public AttributeTypeDescriptionEntity getDescription(
			AttributeTypeDescriptionPK attributeTypeDescriptionPK)
			throws AttributeTypeDescriptionNotFoundException {
		AttributeTypeDescriptionEntity attributeTypeDescription = this.entityManager
				.find(AttributeTypeDescriptionEntity.class,
						attributeTypeDescriptionPK);
		if (null == attributeTypeDescription) {
			throw new AttributeTypeDescriptionNotFoundException();
		}
		return attributeTypeDescription;
	}

	public AttributeTypeDescriptionEntity findDescription(
			AttributeTypeDescriptionPK attributeTypeDescriptionPK) {
		AttributeTypeDescriptionEntity attributeTypeDescription = this.entityManager
				.find(AttributeTypeDescriptionEntity.class,
						attributeTypeDescriptionPK);
		return attributeTypeDescription;
	}

	public Map<String, Long> categorize(ApplicationEntity application,
			AttributeTypeEntity attributeType) {
		Query query = AttributeTypeEntity.createQueryCategorize(entityManager,
				application, attributeType);
		List results = query.getResultList();
		Map<String, Long> result = new HashMap<String, Long>();
		for (Iterator iter = results.iterator(); iter.hasNext();) {
			Object[] values = (Object[]) iter.next();
			result.put((String) values[0], (Long) values[1]);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public AttributeTypeEntity getParent(AttributeTypeEntity memberAttributeType)
			throws AttributeTypeNotFoundException {
		Query query = CompoundedAttributeTypeMemberEntity.createParentQuery(
				this.entityManager, memberAttributeType);
		List<AttributeTypeEntity> result = query.getResultList();
		if (result.isEmpty()) {
			throw new AttributeTypeNotFoundException();
		}
		AttributeTypeEntity parent = result.get(0);
		return parent;
	}
}
