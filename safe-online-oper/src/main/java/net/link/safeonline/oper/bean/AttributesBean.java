/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.Attributes;
import net.link.safeonline.oper.OperatorConstants;
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

	@In(required = false)
	private AttributeTypeEntity newAttributeType;

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
		this.newAttributeType.setType("string");
		try {
			this.attributeTypeService.add(this.newAttributeType);
		} catch (ExistingAttributeTypeException e) {
			String msg = "existing attribute type";
			LOG.debug(msg);
			this.facesMessages.addToControl("name", msg);
			return null;
		}
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory("newAttributeType")
	public AttributeTypeEntity newAttributeTypeFactory() {
		return new AttributeTypeEntity();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		LOG.debug("view: " + this.selectedAttributeType.getName());
		return "view";
	}
}
