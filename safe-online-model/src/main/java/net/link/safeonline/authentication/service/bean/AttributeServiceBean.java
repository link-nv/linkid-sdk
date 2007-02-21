/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;

/**
 * Attribute Service Implementation for applications.
 * 
 * TODO: add application security domain and roles.
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class AttributeServiceBean implements AttributeService {

	@EJB
	private AttributeDAO attributeDAO;

	public String getAttribute(String subjectLogin, String attributeName)
			throws AttributeNotFoundException {
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, subjectLogin);
		if (null == attribute) {
			throw new AttributeNotFoundException();
		}
		String value = attribute.getStringValue();
		return value;
	}
}
