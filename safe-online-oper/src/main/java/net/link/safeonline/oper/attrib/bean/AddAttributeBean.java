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
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.ctrl.Convertor;
import net.link.safeonline.ctrl.ConvertorUtil;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.attrib.AddAttribute;
import net.link.safeonline.service.AttributeTypeService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("addAttribute")
@Scope(ScopeType.CONVERSATION)
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "AddAttributeBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class AddAttributeBean implements AddAttribute {

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@Logger
	private Log log;

	@EJB
	private NodeService nodeService;

	private String category;

	private String name;

	private String node;

	@In(create = true)
	FacesMessages facesMessages;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getCategory() {
		return this.category;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getName() {
		return this.name;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String next() {
		this.log.debug("next: name #0, category #1", this.name, this.category);
		return this.category;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setCategory(String category) {
		this.category = category;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setName(String name) {
		this.name = name;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getNode() {
		return this.node;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setNode(String node) {
		this.node = node;
	}

	@Factory("datatypes")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<SelectItem> datatypesFactory() {
		List<SelectItem> datatypes = new LinkedList<SelectItem>();
		for (DatatypeType currentType : DatatypeType.values()) {
			if (false == currentType.isPrimitive()) {
				continue;
			}
			datatypes.add(new SelectItem(currentType.name(), currentType
					.getFriendlyName()));

		}
		return datatypes;
	}

	@Factory("nodes")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<SelectItem> nodeFactory() {
		List<OlasEntity> nodeList = this.nodeService.listNodes();
		List<SelectItem> nodes = ConvertorUtil.convert(nodeList,
				new OlasEntitySelectItemConvertor());
		return nodes;
	}

	static class OlasEntitySelectItemConvertor implements
			Convertor<OlasEntity, SelectItem> {

		public SelectItem convert(OlasEntity input) {
			SelectItem output = new SelectItem(input.getName());
			return output;
		}
	}

	private String type;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getType() {
		return this.type;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setType(String type) {
		this.type = type;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String typeNext() {
		this.log.debug("type next");
		return "next";
	}

	private boolean userVisible;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public boolean isUserVisible() {
		return this.userVisible;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setUserVisible(boolean userVisible) {
		this.userVisible = userVisible;
	}

	private boolean userEditable;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public boolean isUserEditable() {
		return this.userEditable;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setUserEditable(boolean userEditable) {
		this.userEditable = userEditable;
	}

	@EJB
	private AttributeTypeService attributeTypeService;

	private List<AttributeTypeEntity> sourceMemberAttributes;

	private List<AttributeTypeEntity> selectedMemberAttributes;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<AttributeTypeEntity> getSourceMemberAttributes() {
		this.sourceMemberAttributes = this.attributeTypeService
				.listAvailableMemberAttributeTypes();
		return this.sourceMemberAttributes;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setSourceMemberAttributes(
			List<AttributeTypeEntity> sourceMemberAttributes) {
		this.sourceMemberAttributes = sourceMemberAttributes;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<AttributeTypeEntity> getTargetMemberAttributes() {
		return this.selectedMemberAttributes;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setTargetMemberAttributes(
			List<AttributeTypeEntity> targetMemberAttributes) {
		this.selectedMemberAttributes = targetMemberAttributes;
		for (AttributeTypeEntity targetMemberAttribute : this.selectedMemberAttributes)
			this.log.debug("set target: " + targetMemberAttribute.getName());
	}

	@SuppressWarnings("unused")
	@DataModel
	private List<AttributeTypeEntity> attributeTypeList;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@End
	public String add() {
		this.log.debug("add");

		AttributeTypeEntity attributeType = new AttributeTypeEntity();
		attributeType.setName(this.name);
		if ("compounded".equals(this.category)) {
			attributeType.setMultivalued(true);
			attributeType.setType(DatatypeType.COMPOUNDED);
		} else {
			if ("multiValued".equals(this.category)) {
				attributeType.setMultivalued(true);
			}
			attributeType.setType(DatatypeType.valueOf(this.type));
		}
		attributeType.setUserEditable(this.userEditable);
		attributeType.setUserVisible(this.userVisible);

		try {
			OlasEntity olasNode = this.nodeService.getNode(this.node);
			attributeType.setLocation(olasNode);
		} catch (NodeNotFoundException e) {
			String msg = "olas node not found";
			this.log.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("location",
					FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
			return null;
		}

		if (null != this.memberAccessControlAttributes) {
			int memberSequence = 0;
			for (MemberAccessControl memberAccessControlAttribute : this.memberAccessControlAttributes) {
				this.log.debug("selected member attribute: "
						+ memberAccessControlAttribute.getName());
				attributeType.addMember(memberAccessControlAttribute
						.getAttributeType(), memberSequence,
						memberAccessControlAttribute.isRequired());
				memberSequence += 1;
			}
		}
		try {
			this.attributeTypeService.add(attributeType);
		} catch (ExistingAttributeTypeException e) {
			String msg = "existing attribute type";
			this.log.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorAttributeTypeAlreadyExists");
			return null;
		} catch (AttributeTypeNotFoundException e) {
			String msg = "member attribute type not found";
			this.log.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorAttributeTypeMemberNotFound");
			return null;
		} catch (AttributeTypeDefinitionException e) {
			String msg = "illegal member attribute type";
			this.log.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorAttributeTypeMemberIllegal");
			return null;
		}

		/*
		 * Reload the attribute type list here.
		 */
		this.attributeTypeList = this.attributeTypeService.listAttributeTypes();

		return "success";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String cancel() {
		return "canceled";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String membersNext() {
		this.log.debug("members next");
		return "next";
	}

	public static final String MEMBER_ACCESS_CONTROL_ATTRIBUTES = "memberAccessControlAttributes";

	@DataModel("memberAccessControlAttributes")
	private List<MemberAccessControl> memberAccessControlAttributes;

	public static class MemberAccessControl {
		private boolean required;

		private final AttributeTypeEntity attributeType;

		public MemberAccessControl(AttributeTypeEntity attributeType) {
			this.attributeType = attributeType;
		}

		public String getName() {
			return this.attributeType.getName();
		}

		public boolean isRequired() {
			return this.required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public AttributeTypeEntity getAttributeType() {
			return this.attributeType;
		}
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory(MEMBER_ACCESS_CONTROL_ATTRIBUTES)
	public void memberAccessControlAttributesFactory() {
		this.log.debug("memberAccessControlAttributesFactory");
		this.memberAccessControlAttributes = new LinkedList<MemberAccessControl>();
		if (null == this.selectedMemberAttributes) {
			return;
		}
		for (AttributeTypeEntity selectedMemberAttribute : this.selectedMemberAttributes) {
			this.memberAccessControlAttributes.add(new MemberAccessControl(
					selectedMemberAttribute));
		}
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String membersAccessControlNext() {
		this.log.debug("members access control next");
		return "next";
	}
}
