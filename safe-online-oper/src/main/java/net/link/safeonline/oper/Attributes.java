/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

import net.link.safeonline.entity.AttributeTypeEntity;

@Local
public interface Attributes {

	void attributeTypeListFactory();

	void destroyCallback();

	AttributeTypeEntity newAttributeTypeFactory();

	String add();
}
