/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeProviderPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = AttributeProviderDAO.JNDI_BINDING)
public class AttributeProviderDAOBean implements AttributeProviderDAO {

    private static final Log                       LOG = LogFactory.getLog(AttributeProviderDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                          entityManager;

    private AttributeProviderEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, AttributeProviderEntity.QueryInterface.class);
    }

    public AttributeProviderEntity findAttributeProvider(ApplicationEntity application, AttributeTypeEntity attributeType) {

        AttributeProviderPK pk = new AttributeProviderPK(application, attributeType);
        AttributeProviderEntity attributeProvider = entityManager.find(AttributeProviderEntity.class, pk);
        return attributeProvider;
    }

    public List<AttributeProviderEntity> listAttributeProviders(AttributeTypeEntity attributeType) {

        List<AttributeProviderEntity> attributeProviders = queryObject.listAttributeProviders(attributeType);
        return attributeProviders;
    }

    public void removeAttributeProvider(AttributeProviderEntity attributeProvider) {

        entityManager.remove(attributeProvider);
    }

    public void addAttributeProvider(ApplicationEntity application, AttributeTypeEntity attributeType) {

        AttributeProviderEntity attributeProvider = new AttributeProviderEntity(application, attributeType);
        entityManager.persist(attributeProvider);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void removeAttributeProviders(ApplicationEntity application) {

        int count = queryObject.removeAttributeProviders(application);
        LOG.debug("number of removed provider entities: " + count);
    }

    public void removeAttributeProviders(AttributeTypeEntity attributeType) {

        int count = queryObject.removeAttributeProviders(attributeType);
        LOG.debug("number of removed provider entities: " + count);
    }
}
