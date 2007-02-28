/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.service.AttributeTypeService;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class AttributeTypeServiceBean implements AttributeTypeService {

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AttributeTypeEntity> getAttributeTypes() {
		List<AttributeTypeEntity> attributeTypes = this.attributeTypeDAO
				.getAttributeTypes();
		return attributeTypes;
	}
}
