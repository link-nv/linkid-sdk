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

@Local
public interface AttributeProvider {

	/*
	 * factories
	 */
	void attributeProvidersFactory();

	List<SelectItem> getApplicationList();

	/*
	 * actions
	 */
	String removeProvider();

	String add();

	/*
	 * accessors
	 */
	String getApplication();

	void setApplication(String application);

	/*
	 * lifecycle
	 */
	void destroyCallback();
}
