/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.entity.AttributeTypeEntity;

@Local
@Remote
public interface AttributeTypeService {

	List<AttributeTypeEntity> getAttributeTypes();

	void add(AttributeTypeEntity attributeType)
			throws ExistingAttributeTypeException;
}
