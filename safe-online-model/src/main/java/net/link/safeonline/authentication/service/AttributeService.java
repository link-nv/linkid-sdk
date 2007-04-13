/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.Map;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;

/**
 * Attribute Service. To be used by applications to retrieve attributes of their
 * users. Applications can only retrieve attributes for which the user did
 * confirm the corresponding application identity.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeService {

	/**
	 * Gives back the value of an attribute of a certain subject. The subject
	 * must have confirmed attribute usage before the application is allowed to
	 * access the attribute value.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws PermissionDeniedException
	 * @throws SubjectNotFoundException
	 */
	String getConfirmedAttribute(String subjectLogin, String attributeName)
			throws AttributeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException;

	/**
	 * Returns a map of attributes with values of the given subject. Of course
	 * the subject needs to be subscribed onto the current caller application
	 * and the attributes returned are those that have been confirmed by the
	 * user. It is possible that a user already confirmed an attribute usage
	 * over an attribute that he still needs to define. In this case the
	 * resulting map will not contain an entry for the missing attribute.
	 * 
	 * @param subjectLogin
	 * @return
	 * @throws SubjectNotFoundException
	 * @throws PermissionDeniedException
	 */
	Map<String, String> getConfirmedAttributes(String subjectLogin)
			throws SubjectNotFoundException, PermissionDeniedException;
}