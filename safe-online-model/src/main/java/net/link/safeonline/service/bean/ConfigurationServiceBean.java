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
import net.link.safeonline.dao.ConfigGroupDAO;
import net.link.safeonline.dao.ConfigItemDAO;
import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.service.ConfigurationService;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class ConfigurationServiceBean implements ConfigurationService {

	@EJB
	private ConfigGroupDAO configGroupDAO;

	@EJB
	private ConfigItemDAO configItemDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<ConfigGroupEntity> getConfigGroups() {
		return this.configGroupDAO.listConfigGroups();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void saveConfigItem(ConfigItemEntity configItem) {
		this.configItemDAO.saveConfigItem(configItem);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void saveConfiguration(List<ConfigGroupEntity> configGroupList) {
		for (ConfigGroupEntity configGroup : configGroupList) {
			this.configGroupDAO.saveConfigGroup(configGroup);
			for (ConfigItemEntity configItem : configGroup.getConfigItems()) {
				this.configItemDAO.saveConfigItem(configItem);
			}
		}
	}
}
