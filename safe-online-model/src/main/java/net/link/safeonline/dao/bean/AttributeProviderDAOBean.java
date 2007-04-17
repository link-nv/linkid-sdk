/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeProviderPK;
import net.link.safeonline.entity.AttributeTypeEntity;

@Stateless
public class AttributeProviderDAOBean implements AttributeProviderDAO {

	private static final Log LOG = LogFactory
			.getLog(AttributeProviderDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public AttributeProviderEntity findAttributeProvider(
			ApplicationEntity application, AttributeTypeEntity attributeType) {
		AttributeProviderPK pk = new AttributeProviderPK(application,
				attributeType);
		AttributeProviderEntity attributeProvider = this.entityManager.find(
				AttributeProviderEntity.class, pk);
		return attributeProvider;
	}

	@SuppressWarnings("unchecked")
	public List<AttributeProviderEntity> listAttributeProviders(
			AttributeTypeEntity attributeType) {
		Query query = AttributeProviderEntity.createQueryWhereAttributeType(
				this.entityManager, attributeType);
		List<AttributeProviderEntity> attributeProviders = query
				.getResultList();
		return attributeProviders;
	}

	public void removeAttributeProvider(
			AttributeProviderEntity attributeProvider) {
		this.entityManager.remove(attributeProvider);
	}

	public void addAttributeProvider(ApplicationEntity application,
			AttributeTypeEntity attributeType) {
		AttributeProviderEntity attributeProvider = new AttributeProviderEntity(
				application, attributeType);
		this.entityManager.persist(attributeProvider);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void removeAttributeProviders(ApplicationEntity application) {
		Query query = AttributeProviderEntity.createDeleteWhereApplication(
				this.entityManager, application);
		int count = query.executeUpdate();
		LOG.debug("number of removed provider entities: " + count);
	}
}
