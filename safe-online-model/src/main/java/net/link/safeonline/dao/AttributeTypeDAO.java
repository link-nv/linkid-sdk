/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.entity.AttributeTypeEntity;

@Local
public interface AttributeTypeDAO {

	void addAttributeType(AttributeTypeEntity attributeType);

	AttributeTypeEntity findAttributeType(String name);

	AttributeTypeEntity getAttributeType(String name)
			throws AttributeTypeNotFoundException;

	List<AttributeTypeEntity> getAttributeTypes();
}
