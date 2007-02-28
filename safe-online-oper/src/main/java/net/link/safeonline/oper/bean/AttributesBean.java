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

import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.oper.Attributes;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.service.AttributeTypeService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;

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

	@Factory("attributeTypeList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void attributeTypeListFactory() {
		LOG.debug("attributeTypeListFactory");
		this.attributeTypeList = this.attributeTypeService.getAttributeTypes();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}
}
