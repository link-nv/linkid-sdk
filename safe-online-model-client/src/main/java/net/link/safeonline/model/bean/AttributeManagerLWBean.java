/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJBException;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.data.CompoundAttributeDO;
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
 * This bean is not an EJB3 session bean, but a regular POJO that can be used within the tx/security context of caller session beans. This
 * is an interesting pattern to keep the overhead of tx/security interceptors as low as possible. Applying this pattern all over the place
 * will certainly result in an increase of performance. For lightweight beans we're using the LWBean suffix.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class AttributeManagerLWBean {

    private static final Log       LOG = LogFactory.getLog(AttributeManagerLWBean.class);

    private final AttributeDAO     attributeDAO;
    private final AttributeTypeDAO attributeTypeDAO;


    /**
     * Main constructor. We use constructor-based dependency injection here.
     * 
     * @param attributeDAO
     */
    public AttributeManagerLWBean(AttributeDAO attributeDAO, AttributeTypeDAO attributeTypeDAO) {

        this.attributeDAO = attributeDAO;
        this.attributeTypeDAO = attributeTypeDAO;
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
            AttributeEntity attributeEntity = attributeDAO.getAttribute(attributeType, subject);
            attributeDAO.removeAttribute(attributeEntity);
            return;
        }

        if (attributeType.isCompounded())
            throw new EJBException("cannot remove compounded attributes via this method");

        /*
         * Else we're dealing with multi-valued attributes. In this case we're removing all of the multi-valued entries for the given
         * attribute type.
         */
        List<AttributeEntity> attributes = attributeDAO.listAttributes(subject, attributeType);
        for (AttributeEntity attribute : attributes) {
            attributeDAO.removeAttribute(attribute);
        }
    }

    /**
     * Removes an attribute of the given attribute type for the given subject.
     * 
     * <p>
     * In case of a multi-valued (compounded) attribute, only the attribute entry corresponding with the given attribute index will be
     * removed.
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
     * @throws AttributeNotFoundException
     * @throws AttributeTypeNotFoundException
     */
    public void removeAttribute(AttributeTypeEntity attributeType, long attributeIndex, SubjectEntity subject)
            throws AttributeNotFoundException, AttributeTypeNotFoundException {

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
             * Since the compounded top-level attribute also has a data entry itself we simply continue to remove after all member entries
             * have been removed.
             */
        }

        if (attributeType.isMultivalued()) {
            /*
             * In case the attribute to be removed is part of a multivalued attribute we have to resequence the remaining attributes.
             */
            List<AttributeEntity> attributes = attributeDAO.listAttributes(subject, attributeType);
            if (attributes.isEmpty()) {
                if (attributeType.isCompoundMember())
                    /*
                     * For compounded attributes we allow some optional member attributes to be empty.
                     */
                    return;
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
                if (attributeType.isCompoundMember())
                    /*
                     * For compounded attributes we allow some optional member attributes to be empty.
                     */
                    return;
                throw new AttributeNotFoundException();
            }
            /*
             * We remove by moving the data of the following remaining attributes one up, and finally we remove the last entry in the list.
             */
            while (iterator.hasNext()) {
                AttributeEntity nextAttribute = iterator.next();
                /*
                 * By copying the content of the next attribute into the remove attribute we basically reindex the attributes. We cannot
                 * just change the attribute index since it is part of the compounded primary key of the attribute entity. Maybe we should
                 * use a global PK attribute Id and a separate viewId instead?
                 */
                if (attributeType.isCompounded()) {
                    removeAttribute.setStringValue(nextAttribute.getStringValue());
                } else {
                    removeAttribute.setValue(nextAttribute.getValue());
                }
                removeAttribute = nextAttribute;
            }
            attributeDAO.removeAttribute(removeAttribute);
        }

        else {
            AttributeEntity attributeEntity = attributeDAO.getAttribute(attributeType, subject);
            attributeDAO.removeAttribute(attributeEntity);
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
            AttributeEntity memberAttribute = attributeDAO.findAttribute(subject, memberAttributeType, attributeIdx);
            if (null != memberAttribute) {
                /*
                 * It's allowed that some (i.e. the optional) member attribute entries are missing.
                 */
                attributeDAO.removeAttribute(memberAttribute);
            }
        }

        attributeDAO.removeAttribute(compoundAttribute);
    }

    private AttributeEntity getCompoundAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, String attributeId)
            throws AttributeNotFoundException {

        if (false == attributeType.isCompounded())
            throw new EJBException("not a compounded attribute type");
        List<AttributeEntity> attributes = attributeDAO.listAttributes(subject, attributeType);
        for (AttributeEntity attribute : attributes) {
            if (attributeId.equals(attribute.getStringValue()))
                return attribute;
        }
        throw new AttributeNotFoundException();
    }

    // ---------

    /**
     * @see #newCompound(AttributeTypeEntity, SubjectEntity)
     */
    public CompoundAttributeDO newCompound(String attributeTypeName, SubjectEntity subject)
            throws AttributeTypeNotFoundException {

        AttributeEntity compoundAttribute = newAttribute(attributeTypeName, UUID.randomUUID().toString(), subject);
        return new CompoundAttributeDO(compoundAttribute, this);
    }

    /**
     * Create a compound attribute from the {@link AttributeTypeEntity} with the given name.
     * 
     * @return A {@link CompoundAttributeDO} object which you can use to add members to this compound attribute.
     */
    public CompoundAttributeDO newCompound(AttributeTypeEntity attributeType, SubjectEntity subject) {

        AttributeEntity compoundAttribute = newAttribute(attributeType, UUID.randomUUID().toString(), subject);
        return new CompoundAttributeDO(compoundAttribute, this);
    }

    /**
     * @see #newAttribute(AttributeTypeEntity, Object, SubjectEntity)
     */
    public AttributeEntity newAttribute(String attributeTypeName, Object attributeValue, SubjectEntity subject)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeTypeName);
        return newAttribute(attributeType, attributeValue, subject);
    }

    /**
     * Create a new attribute and initialize it with the given value.
     * 
     * <p>
     * <b>NOTE:</b> Uses the first available index in the attribute type.
     * </p>
     */
    public AttributeEntity newAttribute(AttributeTypeEntity attributeType, Object attributeValue, SubjectEntity subject) {

        AttributeEntity attribute = attributeDAO.addAttribute(attributeType, subject);

        attribute.setValue(attributeValue);

        return attribute;
    }

    /**
     * @see #newAttribute(AttributeTypeEntity, Object, AttributeEntity)
     */
    public AttributeEntity newAttribute(String attributeTypeName, Object attributeValue, AttributeEntity parent)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeTypeName);
        return newAttribute(attributeType, attributeValue, parent);
    }

    /**
     * Create a new attribute and initialize it with the given value.
     * 
     * @param parent
     *            The parent compound attribute that this attribute is a member of (shares index & subject with).
     */
    public AttributeEntity newAttribute(AttributeTypeEntity attributeType, Object attributeValue, AttributeEntity parent) {

        AttributeEntity attribute = attributeDAO.addAttribute(attributeType, parent.getSubject(), parent.getAttributeIndex());
        attribute.setValue(attributeValue);

        return attribute;
    }

    /**
     * @see #removeCompoundWhere(SubjectEntity, AttributeTypeEntity, AttributeTypeEntity, Object)
     */
    public void removeCompoundWhere(SubjectEntity subject, String parentAttributeTypeName, String memberAttributeTypeName,
                                    Object memberValue)
            throws AttributeTypeNotFoundException, AttributeNotFoundException {

        AttributeTypeEntity parentAttributeType = attributeTypeDAO.getAttributeType(parentAttributeTypeName);
        AttributeTypeEntity memberAttributeType = attributeTypeDAO.getAttributeType(memberAttributeTypeName);

        removeCompoundWhere(subject, parentAttributeType, memberAttributeType, memberValue);
    }

    /**
     * Remove the compound attribute and its members where the given member attribute has the given value.
     * 
     * @param parentAttributeType
     *            The attribute type of the parent (compound) attribute to find and remove.
     * @param memberAttributeType
     *            The attribute type of the parent's member attribute which value we're checking memberValue against.
     * @param memberValue
     *            The value that the memberAttributeTypeName's attribute has to have.
     */
    public void removeCompoundWhere(SubjectEntity subject, AttributeTypeEntity parentAttributeType,
                                    AttributeTypeEntity memberAttributeType, Object memberValue)
            throws AttributeTypeNotFoundException, AttributeNotFoundException {

        List<AttributeEntity> parentAttributes = attributeDAO.listAttributes(subject, parentAttributeType);
        for (AttributeEntity parentAttribute : parentAttributes) {
            AttributeEntity memberAttribute = attributeDAO.findAttribute(subject, memberAttributeType, parentAttribute.getAttributeIndex());
            if (memberValue.equals(memberAttribute.getValue())) {
                removeAttribute(parentAttributeType, parentAttribute.getAttributeIndex(), subject);
                break;
            }
        }
    }

    /**
     * @param parentAttribute
     *            The parent compound attribute.
     * 
     * @return All member attributes of the given compound attribute.
     */
    public List<AttributeEntity> getCompoundMembers(AttributeEntity parentAttribute)
            throws AttributeNotFoundException {

        SubjectEntity subject = parentAttribute.getSubject();
        List<AttributeEntity> attributeMembers = new LinkedList<AttributeEntity>();

        for (CompoundedAttributeTypeMemberEntity memberAttributeType : parentAttribute.getAttributeType().getMembers()) {
            attributeMembers.add(attributeDAO.getAttribute(memberAttributeType.getMember(), subject, parentAttribute.getAttributeIndex()));
        }

        return attributeMembers;
    }

    /**
     * @see #getCompoundMember(AttributeEntity, AttributeTypeEntity)
     */
    public AttributeEntity getCompoundMember(AttributeEntity parentAttribute, String memberAttributeTypeName)
            throws AttributeNotFoundException, AttributeTypeNotFoundException {

        AttributeTypeEntity memberAttributeType = attributeTypeDAO.getAttributeType(memberAttributeTypeName);
        return getCompoundMember(parentAttribute, memberAttributeType);
    }

    /**
     * @param parentAttribute
     *            The parent compound attribute.
     * @param memberAttributeType
     *            The name of the attribute type of the member attribute to return.
     * 
     * @return The member attribute of the given compound attribute of the {@link AttributeTypeEntity} with the given name. (
     *         <code>null</code> if the parentAttribute is <code>null</code>)
     */
    public AttributeEntity getCompoundMember(AttributeEntity parentAttribute, AttributeTypeEntity memberAttributeType)
            throws AttributeNotFoundException {

        SubjectEntity subject = parentAttribute.getSubject();

        return attributeDAO.getAttribute(memberAttributeType, subject, parentAttribute.getAttributeIndex());
    }

    /**
     * @see #getCompoundWhere(SubjectEntity, AttributeTypeEntity, AttributeTypeEntity, String)
     */
    public AttributeEntity getCompoundWhere(SubjectEntity subject, String parentAttributeTypeName, String memberAttributeTypeName,
                                            String memberValue)
            throws AttributeTypeNotFoundException, AttributeNotFoundException {

        AttributeTypeEntity parentAttributeType = attributeTypeDAO.getAttributeType(parentAttributeTypeName);
        AttributeTypeEntity memberAttributeType = attributeTypeDAO.getAttributeType(memberAttributeTypeName);

        return getCompoundWhere(subject, parentAttributeType, memberAttributeType, memberValue);
    }

    /**
     * @param parentAttributeType
     *            The attribute type of the parent (compound) attribute to search through & return.
     * @param memberAttributeType
     *            The attribute type of the parent's member attribute which value we're checking memberValue against. If <code>null</code>,
     *            the memberValue is checked against the compound attribute's value (Which is to say, it's the compound attribute's UUID,
     *            attributeId)
     * @param memberValue
     *            The value that the memberAttributeTypeName's attribute has to have for the parent attribute we're returning.
     * 
     * @return The compound attribute whose member contains a certain value.
     */
    public AttributeEntity getCompoundWhere(SubjectEntity subject, AttributeTypeEntity parentAttributeType,
                                            AttributeTypeEntity memberAttributeType, String memberValue)
            throws AttributeNotFoundException {

        AttributeEntity compoundAttribute = findCompoundWhere(subject, parentAttributeType, memberAttributeType, memberValue);
        if (compoundAttribute == null)
            throw new AttributeNotFoundException();

        return compoundAttribute;
    }

    /**
     * @see #findCompoundWhere(SubjectEntity, AttributeTypeEntity, AttributeTypeEntity, String)
     */
    public AttributeEntity findCompoundWhere(SubjectEntity subject, String parentAttributeTypeName, String memberAttributeTypeName,
                                             String memberValue)
            throws AttributeTypeNotFoundException {

        AttributeTypeEntity parentAttributeType = attributeTypeDAO.getAttributeType(parentAttributeTypeName);
        AttributeTypeEntity memberAttributeType = attributeTypeDAO.getAttributeType(memberAttributeTypeName);

        return findCompoundWhere(subject, parentAttributeType, memberAttributeType, memberValue);
    }

    /**
     * @param parentAttributeType
     *            The attribute type of the parent (compound) attribute to search through & return.
     * @param memberAttributeType
     *            The attribute type of the parent's member attribute which value we're checking memberValue against. If <code>null</code>,
     *            the memberValue is checked against the compound attribute's value (Which is to say, it's the compound attribute's UUID,
     *            attributeId)
     * @param memberValue
     *            The value that the memberAttributeTypeName's attribute has to have for the parent attribute we're returning.
     * 
     * @return The compound attribute whose member contains a certain value.
     */
    public AttributeEntity findCompoundWhere(SubjectEntity subject, AttributeTypeEntity parentAttributeType,
                                             AttributeTypeEntity memberAttributeType, String memberValue) {

        List<AttributeEntity> parentAttributes = attributeDAO.listAttributes(subject, parentAttributeType);
        for (AttributeEntity parentAttribute : parentAttributes) {
            if (memberAttributeType == null) {
                if (memberValue.equals(parentAttribute.getValue()))
                    return parentAttribute;
            }

            else {
                AttributeEntity memberAttribute = attributeDAO.findAttribute(subject, memberAttributeType,
                        parentAttribute.getAttributeIndex());
                if (memberValue.equals(memberAttribute.getValue()))
                    return parentAttribute;
            }
        }

        return null;
    }
}
