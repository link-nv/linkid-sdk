/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface AttributeDAO {

	AttributeEntity findAttribute(String attributeTypeName, String subjectLogin);

	AttributeEntity getAttribute(String attributeTypeName, String subjectLogin)
			throws AttributeNotFoundException;

	AttributeEntity addAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, String stringValue);

	List<AttributeEntity> listAttributes(SubjectEntity subject);

	List<AttributeEntity> listVisibleAttributes(SubjectEntity subject);

	/**
	 * Add or Update an attribute. The index is used for multi-valued
	 * attributes.
	 * 
	 * @param attributeType
	 * @param subject
	 * @param index
	 * @param stringValue
	 */
	void addOrUpdateAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index, String stringValue);

	void addOrUpdateAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index, Boolean booleanValue);

	AttributeEntity findAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject);

	AttributeEntity getAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject) throws AttributeNotFoundException;

	/**
	 * Gives back an attribute. Use this method to retrieve a multi-valued
	 * attribute entry via the <code>index</code> parameter. The attributes
	 * are ordered by attribute index.
	 * 
	 * @param attributeType
	 * @param subject
	 * @param index
	 * @return
	 * @throws AttributeNotFoundException
	 */
	AttributeEntity getAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, long index)
			throws AttributeNotFoundException;

	void removeAttribute(AttributeEntity attributeEntity);

	List<AttributeEntity> listAttributes(SubjectEntity subject,
			AttributeTypeEntity attributeType);
}
