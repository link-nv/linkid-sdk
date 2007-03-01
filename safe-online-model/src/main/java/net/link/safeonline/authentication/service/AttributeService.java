/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;

/**
 * Attribute Service. To be used by applications to retrieve attributes of their
 * users.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface AttributeService {

	String getAttribute(String subjectLogin, String attributeName)
			throws AttributeNotFoundException;
}