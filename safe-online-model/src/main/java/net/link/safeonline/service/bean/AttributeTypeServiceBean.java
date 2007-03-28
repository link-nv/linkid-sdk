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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.service.AttributeTypeService;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class AttributeTypeServiceBean implements AttributeTypeService {

	private static final Log LOG = LogFactory
			.getLog(AttributeTypeServiceBean.class);

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AttributeTypeEntity> listAttributeTypes() {
		List<AttributeTypeEntity> attributeTypes = this.attributeTypeDAO
				.listAttributeTypes();
		return attributeTypes;
	}

	@RolesAllowed(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE)
	public void add(AttributeTypeEntity attributeType)
			throws ExistingAttributeTypeException {
		LOG.debug("add: " + attributeType);
		String name = attributeType.getName();
		checkExistingAttributeType(name);
		this.attributeTypeDAO.addAttributeType(attributeType);
	}

	private void checkExistingAttributeType(String name)
			throws ExistingAttributeTypeException {
		AttributeTypeEntity existingAttributeType = this.attributeTypeDAO
				.findAttributeType(name);
		if (null != existingAttributeType) {
			throw new ExistingAttributeTypeException();
		}
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AttributeTypeDescriptionEntity> listDescriptions(
			String attributeTypeName) throws AttributeTypeNotFoundException {
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeTypeName);
		List<AttributeTypeDescriptionEntity> descriptions = this.attributeTypeDAO
				.listDescriptions(attributeType);
		return descriptions;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addDescription(
			AttributeTypeDescriptionEntity newAttributeTypeDescription)
			throws AttributeTypeNotFoundException {
		String attributeTypeName = newAttributeTypeDescription
				.getAttributeTypeName();
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeTypeName);
		this.attributeTypeDAO.addAttributeTypeDescription(attributeType,
				newAttributeTypeDescription);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeDescription(
			AttributeTypeDescriptionEntity attributeTypeDescription)
			throws AttributeTypeDescriptionNotFoundException {
		AttributeTypeDescriptionEntity attachedEntity = this.attributeTypeDAO
				.getDescription(attributeTypeDescription.getPk());
		this.attributeTypeDAO.removeDescription(attachedEntity);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void saveDescription(
			AttributeTypeDescriptionEntity attributeTypeDescription) {
		this.attributeTypeDAO.saveDescription(attributeTypeDescription);
	}
}
