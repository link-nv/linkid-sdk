/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib;

import javax.ejb.Local;

@Local
public interface Attributes {

	/*
	 * Factory.
	 */
	void attributeTypeListFactory();

	/*
	 * Actions.
	 */
	String view();

	String remove();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
