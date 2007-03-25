/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface AttributeDAO {

	AttributeEntity findAttribute(String attributeTypeName, String subjectLogin);

	void addAttribute(String attributeTypeName, String subjectLogin,
			String stringValue);

	void addAttribute(AttributeTypeEntity attributeType, String subjectLogin,
			String stringValue);

	List<AttributeEntity> listAttributes(SubjectEntity subject);

	void addOrUpdateAttribute(String attributeTypeName, String subjectLogin,
			String stringValue);
}
