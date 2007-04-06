/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
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

	void addAttribute(AttributeTypeEntity attributeType, SubjectEntity subject,
			String stringValue);

	List<AttributeEntity> listAttributes(SubjectEntity subject);

	void addOrUpdateAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject, String stringValue);

	AttributeEntity findAttribute(AttributeTypeEntity attributeType,
			SubjectEntity subject);
}
