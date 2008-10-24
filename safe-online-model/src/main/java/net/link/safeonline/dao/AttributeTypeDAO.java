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

import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.NodeEntity;


/**
 * Interface for attribute type data access object.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeTypeDAO {

    void addAttributeType(AttributeTypeEntity attributeType);

    void removeAttributeType(String name);

    AttributeTypeEntity findAttributeType(String name);

    AttributeTypeEntity getAttributeType(String name) throws AttributeTypeNotFoundException;

    List<AttributeTypeEntity> listAttributeTypes();

    /**
     * List all attribute types that are marked user visible.
     * 
     * @return
     */
    List<AttributeTypeEntity> listVisibleAttributeTypes();

    /**
     * Lists all attribute types on the specified olas node.
     * 
     * @param node
     */
    List<AttributeTypeEntity> listAttributeTypes(NodeEntity node);

    /**
     * List all attribute types of specified datatype
     * 
     * @param datatype
     */
    List<AttributeTypeEntity> listAttributeTypes(DatatypeType datatype);

    List<AttributeTypeDescriptionEntity> listDescriptions(AttributeTypeEntity attributeType);

    void addAttributeTypeDescription(AttributeTypeEntity attributeType, AttributeTypeDescriptionEntity newAttributeTypeDescription);

    /**
     * Removes an attribute type description. The entity parameter should be an attached entity.
     * 
     * @param attributeTypeDescription
     */
    void removeDescription(AttributeTypeDescriptionEntity attributeTypeDescription);

    void saveDescription(AttributeTypeDescriptionEntity attributeTypeDescription);

    AttributeTypeDescriptionEntity getDescription(AttributeTypeDescriptionPK attributeTypeDescriptionPK)
                                                                                                        throws AttributeTypeDescriptionNotFoundException;

    AttributeTypeDescriptionEntity findDescription(AttributeTypeDescriptionPK attributeTypeDescriptionPK);

    /**
     * Returns a map containing a list of unique values of an attribute with a count of how many times these values occur
     * 
     * @param attributeType
     */
    Map<Object, Long> categorize(ApplicationEntity application, AttributeTypeEntity attributeType);

    /**
     * Gives back the compounded parent attribute type to which a member attribute type belongs to.
     * 
     * @param memberAttributeType
     * @throws AttributeTypeNotFoundException
     */
    AttributeTypeEntity getParent(AttributeTypeEntity memberAttributeType) throws AttributeTypeNotFoundException;

    CompoundedAttributeTypeMemberEntity getMemberEntry(AttributeTypeEntity memberAttributeType) throws AttributeTypeNotFoundException;

    /**
     * Removes the compounded member attribute type entities ( not the actual member attribute type ) of the given parent.
     * 
     * @param parentAttributeType
     */
    void removeMemberEntries(AttributeTypeEntity parentAttributeType);

}
