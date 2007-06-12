/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.ctrl.Convertor;
import net.link.safeonline.ctrl.ConvertorUtil;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.attrib.Attributes;
import net.link.safeonline.service.AttributeTypeService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("attributes")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "AttributesBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class AttributesBean implements Attributes {

	private static final Log LOG = LogFactory.getLog(AttributesBean.class);

	@EJB
	private AttributeTypeService attributeTypeService;

	@SuppressWarnings("unused")
	@DataModel
	private List<AttributeTypeEntity> attributeTypeList;

	@SuppressWarnings("unused")
	@DataModelSelection
	@Out(value = "selectedAttributeType", required = false, scope = ScopeType.SESSION)
	private AttributeTypeEntity selectedAttributeType;

	public static final String NEW_ATTRIBUTE_TYPE = "newAttributeType";

	@In(value = NEW_ATTRIBUTE_TYPE, required = false)
	private AttributeTypeEntity newAttributeType;

	private AttributeTypeEntity[] selectedMemberAttributes;

	@Factory("attributeTypeList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void attributeTypeListFactory() {
		LOG.debug("attributeTypeListFactory");
		this.attributeTypeList = this.attributeTypeService.listAttributeTypes();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@In(create = true)
	FacesMessages facesMessages;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	// TODO: global operator role should also be used here
	// TODO: configure roles same way as the core
	public String add() {
		LOG.debug("add: " + this.newAttributeType);
		if (null != this.selectedMemberAttributes) {
			int memberSequence = 0;
			for (AttributeTypeEntity selectedMemberAttribute : this.selectedMemberAttributes) {
				LOG.debug("selected member attribute: "
						+ selectedMemberAttribute.getName());
				this.newAttributeType.addMember(selectedMemberAttribute,
						memberSequence);
				memberSequence += 1;
			}
		}
		try {
			this.attributeTypeService.add(this.newAttributeType);
		} catch (ExistingAttributeTypeException e) {
			String msg = "existing attribute type";
			LOG.debug(msg);
			this.facesMessages.addToControl("name", msg);
			return null;
		} catch (AttributeTypeNotFoundException e) {
			String msg = "member attribute type not found";
			LOG.debug(msg);
			this.facesMessages.addToControl("name", msg);
			return null;
		} catch (AttributeTypeDefinitionException e) {
			String msg = "illegal member attribute type";
			LOG.debug(msg);
			this.facesMessages.addToControl("name", msg);
			return null;
		}
		attributeTypeListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory(NEW_ATTRIBUTE_TYPE)
	public AttributeTypeEntity newAttributeTypeFactory() {
		return new AttributeTypeEntity();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		LOG.debug("view: " + this.selectedAttributeType.getName());
		return "view";
	}

	@Factory("datatypes")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<SelectItem> datatypesFactory() {
		List<SelectItem> datatypes = new LinkedList<SelectItem>();
		datatypes.add(new SelectItem("string"));
		datatypes.add(new SelectItem("boolean"));
		return datatypes;
	}

	@Factory("memberAttributes")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<SelectItem> memberAttributesFactory() {
		List<AttributeTypeEntity> memberAttributeTypes = this.attributeTypeService
				.listAvailableMemberAttributeTypes();
		List<SelectItem> memberAttributes = ConvertorUtil.convert(
				memberAttributeTypes, new AttributeConvertor());
		return memberAttributes;
	}

	private static class AttributeConvertor implements
			Convertor<AttributeTypeEntity, SelectItem> {

		public SelectItem convert(AttributeTypeEntity input) {
			String attributeName = input.getName();
			SelectItem output = new SelectItem(input, attributeName);
			return output;
		}
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public AttributeTypeEntity[] getSelectedMemberAttributes() {
		return this.selectedMemberAttributes;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setSelectedMemberAttributes(
			AttributeTypeEntity[] selectedMemberAttributes) {
		this.selectedMemberAttributes = selectedMemberAttributes;
	}
}
