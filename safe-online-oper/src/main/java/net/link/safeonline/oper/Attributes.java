/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.entity.AttributeTypeEntity;

@Local
public interface Attributes {

	/*
	 * Factory.
	 */
	void attributeTypeListFactory();

	AttributeTypeEntity newAttributeTypeFactory();

	List<SelectItem> datatypesFactory();

	/*
	 * Actions.
	 */
	String add();

	String view();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
