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

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.core.FacesMessages;

import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.oper.Configuration;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.service.ConfigurationService;

@Stateful
@Name("configuration")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "ConfigurationBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ConfigurationBean implements Configuration {

	@DataModel("configGroupList")
	@SuppressWarnings("unused")
	private List<ConfigGroupEntity> configGroupList;

	@EJB
	private ConfigurationService configurationService;

	@In(create = true)
	FacesMessages facesMessages;

	@Factory("configGroupList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void configGroupListFactory() {
		this.configGroupList = this.configurationService.listConfigGroups();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		// empty
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() {
		this.configurationService.saveConfiguration(this.configGroupList);
		return "saved";
	}

}
