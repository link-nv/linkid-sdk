/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Data Access Object interface for Attribute entities. It's important to understand the duality of multi-valued
 * attributes. Towards the user web application interface they behave as regular attributes. Towards the application web
 * service interface they behave as weak entities, i.e., they only make sense as part of the set of multi-valued
 * attributes for the given attribute type.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeDAO {

    AttributeEntity addAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, String stringValue);

    /**
     * Adds a new attribute.
     * 
     * <p>
     * For multi-valued attributes a new attribute will be added with attribute index set to MAX(current attribute ids) +
     * 1. Single-valued attributes will of course have an attribute attribute 0.
     * </p>
     * 
     * @param attributeType
     * @param subject
     */
    AttributeEntity addAttribute(AttributeTypeEntity attributeType, SubjectEntity subject);

    /**
     * Add or Update an attribute. The index is used for multi-valued attributes.
     * 
     * @param attributeType
     * @param subject
     * @param index
     * @param stringValue
     */
    void addOrUpdateAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, long index, String stringValue);

    void addOrUpdateAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, long index, Boolean booleanValue);

    /**
     * Creates a new attribute with the given attribute index. This can be used to create new compounded attribute
     * records.
     * 
     * @param attributeType
     * @param subject
     * @param index
     */
    AttributeEntity addAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, long index);

    void removeAttribute(AttributeEntity attributeEntity);

    /**
     * Gives back an attribute. Use this method to retrieve a multi-valued attribute entry via the <code>index</code>
     * parameter. The attributes are ordered by attribute index.
     * 
     * @param attributeType
     * @param subject
     * @param index
     * @throws AttributeNotFoundException
     */
    AttributeEntity getAttribute(AttributeTypeEntity attributeType, SubjectEntity subject, long index)
            throws AttributeNotFoundException;

    AttributeEntity getAttribute(AttributeTypeEntity attributeType, SubjectEntity subject)
            throws AttributeNotFoundException;

    AttributeEntity getAttribute(String attributeTypeName, SubjectEntity subject) throws AttributeNotFoundException;

    AttributeEntity findAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, long index);

    AttributeEntity findAttribute(SubjectEntity subject, String attributeTypeName, long index);

    AttributeEntity findAttribute(String attributeTypeName, SubjectEntity subject);

    AttributeEntity findAttribute(AttributeTypeEntity attributeType, SubjectEntity subject);

    /**
     * Lists all the attributes of a user. The returned attributes have already been sorted out per attribute type.
     * 
     * @param subject
     */
    Map<AttributeTypeEntity, List<AttributeEntity>> listAttributes(SubjectEntity subject);

    /**
     * Lists all the user visible attributes of a user. The returned attributes have been sorted per attribute type and
     * per attribute index.
     * 
     * @param subject
     */
    List<AttributeEntity> listVisibleAttributes(SubjectEntity subject);

    /**
     * Gives back all attributes of the given attribute type for a certain subject. In case of a multivalued attribute
     * multiple entries can be found in the returned list. These entries will be ordered by attribute index.
     * 
     * @param subject
     * @param attributeType
     */
    List<AttributeEntity> listAttributes(SubjectEntity subject, AttributeTypeEntity attributeType);

    /**
     * Gives back all attributes of the given attribute type which string value starts with the specified prefix.
     * 
     * @param prefix
     * @param attributeType
     */
    List<AttributeEntity> listAttributes(String prefix, AttributeTypeEntity attributeType);

    /**
     * Removes all the attributes of the given subject.
     * 
     * @param subject
     */
    void removeAttributes(SubjectEntity subject);

    /**
     * Removes all the attributes of the given attribue type.
     * 
     * @param attributeType
     */
    void removeAttributes(AttributeTypeEntity attributeType);

}
