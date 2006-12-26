/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.AttributeTypeEntity;

@Local
public interface AttributeTypeDAO {

	void addAttributeType(String name, String type);

	AttributeTypeEntity findAttributeType(String name);
}
