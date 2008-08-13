/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.SubjectEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Lightweight attribute manager bean. The attribute manager is responsible for lifecycle management of attributes.
 * 
 * <p>
 * This bean is not an EJB3 session bean, but a regular POJO that can be used within the tx/security context of caller
 * session beans. This is an interesting pattern to keep the overhead of tx/security interceptors as low as possible.
 * Applying this pattern all over the place will certainly result in an increase of performance. For lightweight beans
 * we're using the LWBean suffix.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class AttributeManagerLWBean {

    private static final Log   LOG = LogFactory.getLog(AttributeManagerLWBean.class);

    private final AttributeDAO attributeDAO;


    /**
     * Main constructor. We use constructor-based dependency injection here.
     * 
     * @param attributeDAO
     */
    public AttributeManagerLWBean(AttributeDAO attributeDAO) {

        this.attributeDAO = attributeDAO;
    }

    /**
     * Removes an attribute of the given attribute type for the given subject.
     * 
     * <p>
     * At this point the RBAC and owner access control checks should already have been performed.
     * </p>
     * 
     * @param attributeType
     * @param subject
     * @throws AttributeNotFoundException
     */
    public void removeAttribute(AttributeTypeEntity attributeType, SubjectEntity subject)
            throws AttributeNotFoundException {

        boolean multivalued = attributeType.isMultivalued();
        if (false == multivalued) {
            AttributeEntity attributeEntity = this.attributeDAO.getAttribute(attributeType, subject);
            this.attributeDAO.removeAttribute(attributeEntity);
            return;
        }

        if (attributeType.isCompounded()) {
            throw new EJBException("cannot remove compounded attributes via this method");
        }

        /*
         * Else we're dealing with multi-valued attributes. In this case we're removing all of the multi-valued entries
         * for the given attribute type.
         */
        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, attributeType);
        for (AttributeEntity attribute : attributes) {
            this.attributeDAO.removeAttribute(attribute);
        }
    }

    /**
     * Removes an attribute of the given attribute type for the given subject.
     * 
     * <p>
     * In case of a multi-valued (compounded) attribute, only the attribute entry corresponding with the given attribute
     * index will be removed.
     * </p>
     * 
     * <p>
     * At this point the RBAC and owner access control checks should already have been performed.
     * </p>
     * 
     * @param attributeType
     * @param attributeIndex
     *            the index of the attribute entry to be removed in case of a multi-valued (compounded) attribute.
     * @param subject
     * @throws PermissionDeniedException
     * @throws AttributeNotFoundException
     * @throws AttributeTypeNotFoundException
     */
    public void removeAttribute(AttributeTypeEntity attributeType, long attributeIndex, SubjectEntity subject)
            throws PermissionDeniedException, AttributeNotFoundException, AttributeTypeNotFoundException {

        LOG.debug("remove attribute " + attributeType.getName() + " for entity with login " + subject);

        if (attributeType.isCompounded()) {
            LOG.debug("remove compounded attribute record for: " + attributeType.getName());
            List<CompoundedAttributeTypeMemberEntity> members = attributeType.getMembers();
            for (CompoundedAttributeTypeMemberEntity member : members) {
                AttributeTypeEntity memberAttributeType = member.getMember();
                /*
                 * We use simple recursion in this case.
                 */
                removeAttribute(memberAttributeType, attributeIndex, subject);
            }
            /*
             * Since the compounded top-level attribute also has a data entry itself we simply continue to remove after
             * all member entries have been removed.
             */
        }

        boolean multivalued = attributeType.isMultivalued();
        if (false == multivalued) {
            AttributeEntity attributeEntity = this.attributeDAO.getAttribute(attributeType, subject);
            this.attributeDAO.removeAttribute(attributeEntity);
        } else {
            /*
             * In case the attribute to be removed is part of a multivalued attribute we have to resequence the
             * remaining attributes.
             */
            List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, attributeType);
            if (attributes.isEmpty()) {
                if (attributeType.isCompoundMember()) {
                    /*
                     * For compounded attributes we allow some optional member attributes to be empty.
                     */
                    return;
                }
                throw new AttributeNotFoundException();
            }
            Iterator<AttributeEntity> iterator = attributes.iterator();
            AttributeEntity removeAttribute = null;
            while (iterator.hasNext()) {
                AttributeEntity iterAttribute = iterator.next();
                if (attributeIndex == iterAttribute.getAttributeIndex()) {
                    removeAttribute = iterAttribute;
                    break;
                }
            }
            if (null == removeAttribute) {
                if (attributeType.isCompoundMember()) {
                    /*
                     * For compounded attributes we allow some optional member attributes to be empty.
                     */
                    return;
                }
                throw new AttributeNotFoundException();
            }
            /*
             * We remove by moving the data of the following remaining attributes one up, and finally we remove the last
             * entry in the list.
             */
            while (iterator.hasNext()) {
                AttributeEntity nextAttribute = iterator.next();
                /*
                 * By copying the content of the next attribute into the remove attribute we basically reindex the
                 * attributes. We cannot just change the attribute index since it is part of the compounded primary key
                 * of the attribute entity. Maybe we should use a global PK attribute Id and a separate viewId instead?
                 */
                removeAttribute.setBooleanValue(nextAttribute.getBooleanValue());
                removeAttribute.setStringValue(nextAttribute.getStringValue());
                removeAttribute = nextAttribute;
            }
            this.attributeDAO.removeAttribute(removeAttribute);
        }
    }

    /**
     * Removes a compounded attribute record of the given attribute type for the given subject.
     * 
     * <p>
     * The compounded attribute record is identified via the attribute Id.
     * </p>
     * 
     * <p>
     * At this point the RBAC and owner access control checks should already have been performed.
     * </p>
     * 
     * @param attributeType
     * @param subject
     * @param attributeId
     * @throws AttributeNotFoundException
     */
    public void removeCompoundAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, String attributeId)
            throws AttributeNotFoundException {

        AttributeEntity compoundAttribute = getCompoundAttribute(attributeType, subject, attributeId);
        long attributeIdx = compoundAttribute.getAttributeIndex();
        LOG.debug("attribute index: " + attributeIdx);
        List<CompoundedAttributeTypeMemberEntity> members = attributeType.getMembers();
        for (CompoundedAttributeTypeMemberEntity member : members) {
            AttributeTypeEntity memberAttributeType = member.getMember();
            AttributeEntity memberAttribute = this.attributeDAO.findAttribute(subject, memberAttributeType,
                    attributeIdx);
            if (null != memberAttribute) {
                /*
                 * It's allowed that some (i.e. the optional) member attribute entries are missing.
                 */
                this.attributeDAO.removeAttribute(memberAttribute);
            }
        }

        this.attributeDAO.removeAttribute(compoundAttribute);
    }

    private AttributeEntity getCompoundAttribute(AttributeTypeEntity attributeType, SubjectEntity subject,
            String attributeId) throws AttributeNotFoundException {

        if (false == attributeType.isCompounded()) {
            throw new EJBException("not a compounded attribute type");
        }
        List<AttributeEntity> attributes = this.attributeDAO.listAttributes(subject, attributeType);
        for (AttributeEntity attribute : attributes) {
            if (attributeId.equals(attribute.getStringValue())) {
                return attribute;
            }
        }
        throw new AttributeNotFoundException();
    }
}
