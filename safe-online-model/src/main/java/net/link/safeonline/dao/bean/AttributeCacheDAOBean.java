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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AttributeCacheDAO;
import net.link.safeonline.entity.AttributeCacheEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class AttributeCacheDAOBean implements AttributeCacheDAO {

    private static final Log                    LOG = LogFactory.getLog(AttributeCacheDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                       entityManager;

    private AttributeCacheEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                AttributeCacheEntity.QueryInterface.class);
    }

    /**
     * {@inheritDoc}
     */
    public List<AttributeCacheEntity> listAttributes(SubjectEntity subject, AttributeTypeEntity attributeType) {

        LOG.debug("listAttributes for " + subject.getUserId() + " of type " + attributeType.getName());
        return this.queryObject.listAttributes(subject, attributeType);
    }

    /**
     * {@inheritDoc}
     */
    public AttributeCacheEntity findAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, long index) {

        LOG.debug("find cached attribute for type  " + attributeType.getName() + " and subject " + subject.getUserId()
                + " (index=" + index + ")");
        AttributeCacheEntity attribute = this.entityManager.find(AttributeCacheEntity.class, new AttributePK(
                attributeType, subject, index));
        return attribute;
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttributes(SubjectEntity subject, AttributeTypeEntity attributeType) {

        List<AttributeCacheEntity> attributes = listAttributes(subject, attributeType);
        for (AttributeCacheEntity attribute : attributes) {
            if (attributeType.isCompounded()) {
                for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
                    AttributeTypeEntity memberAttributeType = member.getMember();
                    AttributeCacheEntity memberAttribute = findAttribute(subject, memberAttributeType, attribute
                            .getAttributeIndex());
                    if (null != memberAttribute) {
                        this.entityManager.remove(memberAttribute);
                    }
                }
            }
            this.entityManager.remove(attribute);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AttributeCacheEntity addAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, long index) {

        LOG.debug("add cached attribute " + attributeType.getName() + " for subject " + subject);
        AttributeCacheEntity attribute = new AttributeCacheEntity(attributeType, subject, index);
        this.entityManager.persist(attribute);
        return attribute;
    }

    /**
     * {@inheritDoc}
     */
    public List<AttributeCacheEntity> listAttributes() {

        LOG.debug("list all cached attributes");
        return this.queryObject.listAttributes();
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttribute(AttributeCacheEntity attribute) {

        LOG.debug("remove attribute of type " + attribute.getAttributeType().getName());
        if (attribute.getAttributeType().isCompounded()) {
            for (CompoundedAttributeTypeMemberEntity member : attribute.getAttributeType().getMembers()) {
                AttributeTypeEntity memberAttributeType = member.getMember();
                AttributeCacheEntity memberAttribute = findAttribute(attribute.getSubject(), memberAttributeType,
                        attribute.getAttributeIndex());
                if (null != memberAttribute) {
                    this.entityManager.remove(memberAttribute);
                }
            }
        }
        this.entityManager.remove(attribute);
    }
}
