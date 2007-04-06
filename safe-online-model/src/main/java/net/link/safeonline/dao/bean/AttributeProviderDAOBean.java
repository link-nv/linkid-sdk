/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeProviderPK;
import net.link.safeonline.entity.AttributeTypeEntity;

@Stateless
public class AttributeProviderDAOBean implements AttributeProviderDAO {

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
}
