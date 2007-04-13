/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.AttributeEntity;

/**
 * Interface for attribute provider service component. The application using
 * this component must be registered as an attribute provider by the operator.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeProviderService {

	/**
	 * Gives back the value of an attribute of a certain subject. This
	 * application must be an attribute provider of the attribute in order to
	 * read the attribute's value.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @return
	 * @throws AttributeTypeNotFoundException
	 * @throws PermissionDeniedException
	 *             if the caller application is not an attribute provider for
	 *             the given attribute.
	 * @throws SubjectNotFoundException
	 */
	AttributeEntity getAttribute(String subjectLogin, String attributeName)
			throws AttributeTypeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException;

	/**
	 * Create a new attribute for the given user.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @param attributeValue
	 * @throws AttributeTypeNotFoundException
	 * @throws PermissionDeniedException
	 * @throws SubjectNotFoundException
	 */
	void createAttribute(String subjectLogin, String attributeName,
			String attributeValue) throws AttributeTypeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException;

	/**
	 * Sets an attribute for the given user.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @param attributeValue
	 * @throws PermissionDeniedException
	 * @throws AttributeTypeNotFoundException
	 * @throws SubjectNotFoundException
	 * @throws AttributeNotFoundException
	 */
	void setAttribute(String subjectLogin, String attributeName,
			String attributeValue) throws AttributeTypeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException,
			AttributeNotFoundException;
}
