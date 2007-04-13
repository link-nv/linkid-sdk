/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;

@Local
public interface AttributeTypeService {

	List<AttributeTypeEntity> listAttributeTypes();

	void add(AttributeTypeEntity attributeType)
			throws ExistingAttributeTypeException;

	List<AttributeTypeDescriptionEntity> listDescriptions(
			String attributeTypeName) throws AttributeTypeNotFoundException;

	/**
	 * Adds a new attribute type description. The entity parameter is used as
	 * data object between the operator control beans and the model service.
	 * 
	 * @param newAttributeTypeDescription
	 * @throws AttributeTypeNotFoundException
	 */
	void addDescription(
			AttributeTypeDescriptionEntity newAttributeTypeDescription)
			throws AttributeTypeNotFoundException;

	void removeDescription(
			AttributeTypeDescriptionEntity attributeTypeDescription)
			throws AttributeTypeDescriptionNotFoundException;

	void saveDescription(AttributeTypeDescriptionEntity attributeTypeDescription);
}
