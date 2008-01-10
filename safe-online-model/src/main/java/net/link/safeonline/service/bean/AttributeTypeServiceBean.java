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

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.AttributeTypeServiceRemote;
import net.link.safeonline.util.Filter;
import net.link.safeonline.util.FilterUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class AttributeTypeServiceBean implements AttributeTypeService,
		AttributeTypeServiceRemote {

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
			throws ExistingAttributeTypeException,
			AttributeTypeNotFoundException, AttributeTypeDefinitionException {
		LOG.debug("add: " + attributeType);
		String name = attributeType.getName();
		checkExistingAttributeType(name);
		checkCompoundedMembers(attributeType);
		this.attributeTypeDAO.addAttributeType(attributeType);
		markCompoundMembers(attributeType);
	}

	private void markCompoundMembers(AttributeTypeEntity attributeType)
			throws AttributeTypeNotFoundException {
		for (CompoundedAttributeTypeMemberEntity member : attributeType
				.getMembers()) {
			String memberName = member.getMember().getName();
			/*
			 * Make sure to first load an attached member attribute type.
			 */
			AttributeTypeEntity memberAttributeType = this.attributeTypeDAO
					.getAttributeType(memberName);
			memberAttributeType.setCompoundMember(true);
		}
	}

	private void checkCompoundedMembers(AttributeTypeEntity attributeType)
			throws AttributeTypeNotFoundException,
			AttributeTypeDefinitionException {
		for (CompoundedAttributeTypeMemberEntity member : attributeType
				.getMembers()) {
			String memberName = member.getMember().getName();
			AttributeTypeEntity memberAttributeType = this.attributeTypeDAO
					.getAttributeType(memberName);
			if (memberAttributeType.isCompounded()) {
				/*
				 * We don't allow compounded of compounded attributes.
				 */
				throw new AttributeTypeDefinitionException();
			}
			if (memberAttributeType.isCompoundMember()) {
				/*
				 * For the moment we don't allow an attribute to be a member of
				 * more than one compounded attribute.
				 */
				throw new AttributeTypeDefinitionException();
			}
		}
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

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AttributeTypeEntity> listAvailableMemberAttributeTypes() {
		List<AttributeTypeEntity> attributeTypes = this.attributeTypeDAO
				.listAttributeTypes();
		List<AttributeTypeEntity> availableMemberAttributeTypes = FilterUtil
				.filter(attributeTypes,
						new AvailableMemberAttributeTypeFilter());
		return availableMemberAttributeTypes;
	}

	static class AvailableMemberAttributeTypeFilter implements
			Filter<AttributeTypeEntity> {

		public boolean isAllowed(AttributeTypeEntity element) {
			if (element.isCompounded()) {
				return false;
			}
			if (element.isCompoundMember()) {
				return false;
			}
			if (false == element.isMultivalued()) {
				return false;
			}
			return true;
		}
	}

	@RolesAllowed(SafeOnlineRoles.GLOBAL_OPERATOR_ROLE)
	public void remove(AttributeTypeEntity attributeType) {
		LOG.debug("remove attribute type: " + attributeType.getName());
		// TODO Auto-generated method stub

	}
}
