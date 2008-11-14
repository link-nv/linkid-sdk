/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.config.dao.ConfigGroupDAO;
import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.config.service.ConfigurationService;
import net.link.safeonline.config.service.ConfigurationServiceRemote;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = ConfigurationService.JNDI_BINDING)
@RemoteBinding(jndiBinding = ConfigurationServiceRemote.JNDI_BINDING)
public class ConfigurationServiceBean implements ConfigurationService, ConfigurationServiceRemote {

    @EJB(mappedName = ConfigGroupDAO.JNDI_BINDING)
    private ConfigGroupDAO configGroupDAO;

    @EJB(mappedName = ConfigItemDAO.JNDI_BINDING)
    private ConfigItemDAO  configItemDAO;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ConfigGroupEntity> listConfigGroups() {

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
