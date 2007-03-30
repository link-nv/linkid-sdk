/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;

/**
 * Interface for attribute type data access object.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeTypeDAO {

	void addAttributeType(AttributeTypeEntity attributeType);

	AttributeTypeEntity findAttributeType(String name);

	AttributeTypeEntity getAttributeType(String name)
			throws AttributeTypeNotFoundException;

	List<AttributeTypeEntity> listAttributeTypes();

	List<AttributeTypeDescriptionEntity> listDescriptions(
			AttributeTypeEntity attributeType);

	void addAttributeTypeDescription(AttributeTypeEntity attributeType,
			AttributeTypeDescriptionEntity newAttributeTypeDescription);

	/**
	 * Removes an attribute type description. The entity parameter should be an
	 * attached entity.
	 * 
	 * @param attributeTypeDescription
	 */
	void removeDescription(
			AttributeTypeDescriptionEntity attributeTypeDescription);

	void saveDescription(AttributeTypeDescriptionEntity attributeTypeDescription);

	AttributeTypeDescriptionEntity getDescription(
			AttributeTypeDescriptionPK attributeTypeDescriptionPK)
			throws AttributeTypeDescriptionNotFoundException;

	AttributeTypeDescriptionEntity findDescription(
			AttributeTypeDescriptionPK attributeTypeDescriptionPK);
}
