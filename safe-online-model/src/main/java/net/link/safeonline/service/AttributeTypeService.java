/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;


@Local
public interface AttributeTypeService {

    /**
     * Lists all attributes types within the system. This includes primitive attribute types, multi-valued attribute
     * types and compounded attribute types.
     * 
     */
    List<AttributeTypeEntity> listAttributeTypes();

    /**
     * Lists all attribute types within the system of specified datatype.
     * 
     * @param datatype
     */
    List<AttributeTypeEntity> listAttributeTypes(DatatypeType datatype);

    /**
     * Lists attribute types that could participate as member in a compounded attribute type.
     * 
     * <p>
     * Via this method we express the restriction that one cannot construct compounded attributes of other compounded
     * attributes. We also don't allow an attribute type to be member of more than one compounded attribute type.
     * </p>
     * 
     */
    List<AttributeTypeEntity> listAvailableMemberAttributeTypes();

    /**
     * Adds a new attribute type using the given attribute type prototype.
     * 
     * @param attributeType
     *            the attribute type prototype.
     * @throws ExistingAttributeTypeException
     * @throws AttributeTypeDefinitionException
     *             in case the member attribute is not allowed when defining a new compounded attribute type.
     * @throws AttributeTypeNotFoundException
     *             in case the member attribute was not found when defining a new compounded attribute type.
     */
    void add(AttributeTypeEntity attributeType) throws ExistingAttributeTypeException, AttributeTypeNotFoundException,
            AttributeTypeDefinitionException;

    /**
     * Removes an attribute type using the given attribute type prototype. No existing application identities should be
     * using this type.
     * 
     * @param attributeType
     * @throws AttributeTypeDescriptionNotFoundException
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     */
    void remove(AttributeTypeEntity attributeType) throws AttributeTypeDescriptionNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException;

    List<AttributeTypeDescriptionEntity> listDescriptions(String attributeTypeName)
            throws AttributeTypeNotFoundException;

    /**
     * Adds a new attribute type description. The entity parameter is used as data object between the operator control
     * beans and the model service.
     * 
     * @param newAttributeTypeDescription
     * @throws AttributeTypeNotFoundException
     */
    void addDescription(AttributeTypeDescriptionEntity newAttributeTypeDescription)
            throws AttributeTypeNotFoundException;

    void removeDescription(AttributeTypeDescriptionEntity attributeTypeDescription)
            throws AttributeTypeDescriptionNotFoundException;

    void saveDescription(AttributeTypeDescriptionEntity attributeTypeDescription);

    void savePluginConfiguration(String attributeTypeName, String pluginConfiguration)
            throws AttributeTypeNotFoundException;

    void saveCacheTimeout(String attributeTypeName, Long cacheTimeout) throws AttributeTypeNotFoundException;
}
