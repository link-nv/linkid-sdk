/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
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
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class AttributeTypeDAOBean implements AttributeTypeDAO {

    private static final Log                                   LOG = LogFactory.getLog(AttributeTypeDAOBean.class);

    private AttributeTypeDescriptionEntity.QueryInterface      descriptorQueryObject;

    private AttributeTypeEntity.QueryInterface                 queryObject;

    private CompoundedAttributeTypeMemberEntity.QueryInterface compoundedQueryObject;

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                                      entityManager;


    @PostConstruct
    public void postConstructCallback() {

        this.descriptorQueryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                AttributeTypeDescriptionEntity.QueryInterface.class);
        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                AttributeTypeEntity.QueryInterface.class);
        this.compoundedQueryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                CompoundedAttributeTypeMemberEntity.QueryInterface.class);
    }

    public void addAttributeType(AttributeTypeEntity attributeType) {

        LOG.debug("add attribute type: " + attributeType.getName());
        this.entityManager.persist(attributeType);
    }

    public void removeAttributeType(String name) {

        LOG.debug("remove attribute type: " + name);
        AttributeTypeEntity attributeType = this.entityManager.find(AttributeTypeEntity.class, name);
        this.entityManager.remove(attributeType);
    }

    public AttributeTypeEntity findAttributeType(String name) {

        LOG.debug("find attribute type: " + name);
        AttributeTypeEntity attributeType = this.entityManager.find(AttributeTypeEntity.class, name);
        return attributeType;
    }

    public List<AttributeTypeEntity> listAttributeTypes() {

        LOG.debug("get attribute types");
        List<AttributeTypeEntity> attributeTypes = this.queryObject.listAttributeTypes();
        return attributeTypes;
    }

    public List<AttributeTypeEntity> listAttributeTypes(NodeEntity node) {

        LOG.debug("get attribute types on node " + node.getName());
        List<AttributeTypeEntity> attributeTypes = this.queryObject.listAttributeTypes(node);
        return attributeTypes;
    }

    public List<AttributeTypeEntity> listVisibleAttributeTypes() {

        LOG.debug("get user visible attribute types");
        List<AttributeTypeEntity> attributeTypes = this.queryObject.listVisibleAttributeTypes();
        return attributeTypes;
    }

    public AttributeTypeEntity getAttributeType(String name) throws AttributeTypeNotFoundException {

        LOG.debug("get attribute type: " + name);
        AttributeTypeEntity attributeType = findAttributeType(name);
        if (null == attributeType)
            throw new AttributeTypeNotFoundException();
        return attributeType;
    }

    public List<AttributeTypeDescriptionEntity> listDescriptions(AttributeTypeEntity attributeType) {

        List<AttributeTypeDescriptionEntity> descriptions = this.descriptorQueryObject.listDescriptions(attributeType);
        return descriptions;
    }

    public void addAttributeTypeDescription(AttributeTypeEntity attributeType,
            AttributeTypeDescriptionEntity newAttributeTypeDescription) {

        /*
         * Manage relationships.
         */
        newAttributeTypeDescription.setAttributeType(attributeType);
        attributeType.getDescriptions().put(newAttributeTypeDescription.getLanguage(), newAttributeTypeDescription);
        /*
         * Persist.
         */
        this.entityManager.persist(newAttributeTypeDescription);
    }

    public void removeDescription(AttributeTypeDescriptionEntity attributeTypeDescription) {

        /*
         * Manage relationships.
         */
        String language = attributeTypeDescription.getLanguage();
        attributeTypeDescription.getAttributeType().getDescriptions().remove(language);
        /*
         * Remove from database.
         */
        this.entityManager.remove(attributeTypeDescription);
    }

    public void saveDescription(AttributeTypeDescriptionEntity attributeTypeDescription) {

        this.entityManager.merge(attributeTypeDescription);
    }

    public AttributeTypeDescriptionEntity getDescription(AttributeTypeDescriptionPK attributeTypeDescriptionPK)
            throws AttributeTypeDescriptionNotFoundException {

        AttributeTypeDescriptionEntity attributeTypeDescription = this.entityManager.find(
                AttributeTypeDescriptionEntity.class, attributeTypeDescriptionPK);
        if (null == attributeTypeDescription)
            throw new AttributeTypeDescriptionNotFoundException();
        return attributeTypeDescription;
    }

    public AttributeTypeDescriptionEntity findDescription(AttributeTypeDescriptionPK attributeTypeDescriptionPK) {

        AttributeTypeDescriptionEntity attributeTypeDescription = this.entityManager.find(
                AttributeTypeDescriptionEntity.class, attributeTypeDescriptionPK);
        return attributeTypeDescription;
    }

    public Map<Object, Long> categorize(ApplicationEntity application, AttributeTypeEntity attributeType) {

        LOG.debug("categorize: " + attributeType.getName());
        Query query;
        if (attributeType.getType().equals(DatatypeType.STRING)) {
            query = this.queryObject.createQueryCategorizeString(application, attributeType);
        } else if (attributeType.getType().equals(DatatypeType.LOGIN)) {
            query = this.queryObject.createQueryCategorizeLogin(application, attributeType);
        } else if (attributeType.getType().equals(DatatypeType.BOOLEAN)) {
            query = this.queryObject.createQueryCategorizeBoolean(application, attributeType);
        } else if (attributeType.getType().equals(DatatypeType.INTEGER)) {
            query = this.queryObject.createQueryCategorizeInteger(application, attributeType);
        } else if (attributeType.getType().equals(DatatypeType.DOUBLE)) {
            query = this.queryObject.createQueryCategorizeDouble(application, attributeType);
        } else if (attributeType.getType().equals(DatatypeType.DATE)) {
            query = this.queryObject.createQueryCategorizeDate(application, attributeType);
        } else
            return null;
        List<?> results = query.getResultList();
        Map<Object, Long> result = new HashMap<Object, Long>();
        for (Object name : results) {
            Object[] values = (Object[]) name;
            result.put(values[0], (Long) values[1]);
        }
        return result;
    }

    public AttributeTypeEntity getParent(AttributeTypeEntity memberAttributeType) throws AttributeTypeNotFoundException {

        AttributeTypeEntity parent = this.compoundedQueryObject.findParentAttribute(memberAttributeType);
        if (null == parent)
            throw new AttributeTypeNotFoundException();
        return parent;
    }

    public CompoundedAttributeTypeMemberEntity getMemberEntry(AttributeTypeEntity memberAttributeType)
            throws AttributeTypeNotFoundException {

        List<CompoundedAttributeTypeMemberEntity> memberEntries = this.compoundedQueryObject
                .listMemberEntries(memberAttributeType);
        if (memberEntries.isEmpty())
            throw new AttributeTypeNotFoundException();
        CompoundedAttributeTypeMemberEntity memberEntry = memberEntries.get(0);
        return memberEntry;
    }

    public void removeMemberEntries(AttributeTypeEntity parentAttributeType) {

        int count = this.compoundedQueryObject.deleteWhereParent(parentAttributeType);
        LOG.debug("number of removed CompoundedAttributeTypeMemberEntity's: " + count);
    }
}
