/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

@Local
public interface ApplicationOwner {

	void applicationOwnerListFactory();

	String getLogin();

	void setLogin(String login);

	String getName();

	void setName(String name);

	String add();

	void destroyCallback();
}
