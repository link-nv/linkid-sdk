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
import net.link.safeonline.config.dao.ConfigItemValueDAO;
import net.link.safeonline.config.service.ConfigurationService;
import net.link.safeonline.config.service.ConfigurationServiceRemote;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = ConfigurationService.JNDI_BINDING)
@RemoteBinding(jndiBinding = ConfigurationServiceRemote.JNDI_BINDING)
public class ConfigurationServiceBean implements ConfigurationService, ConfigurationServiceRemote {

    private static final Log   LOG = LogFactory.getLog(ConfigurationServiceBean.class);

    @EJB(mappedName = ConfigGroupDAO.JNDI_BINDING)
    private ConfigGroupDAO     configGroupDAO;

    @EJB(mappedName = ConfigItemDAO.JNDI_BINDING)
    private ConfigItemDAO      configItemDAO;

    @EJB(mappedName = ConfigItemValueDAO.JNDI_BINDING)
    private ConfigItemValueDAO configItemValueDAO;


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
            LOG.debug("save group: " + configGroup.getName());
            for (ConfigItemEntity configItem : configGroup.getConfigItems()) {
                LOG.debug("save item: " + configItem.getName());
                List<ConfigItemValueEntity> configItemValues = this.configItemValueDAO.listConfigItemValues(configItem);
                if (configItem.isMultipleChoice()) {
                    LOG.debug("save multiple choice");
                    ConfigItemValueEntity configItemValue = configItem.getValue();
                    LOG.debug("selected value: " + configItemValue.getValue());
                    int index = configItemValues.indexOf(configItemValue);
                    configItem = this.configItemDAO.findConfigItem(configGroup.getName(), configItem.getName());
                    if (index != -1) {
                        configItem.setValueIndex(index);
                    }

                } else {
                    LOG.debug("save single");
                    int idx = 0;
                    for (ConfigItemValueEntity configItemValue : configItemValues) {
                        LOG.debug("save value: " + configItem.getValues().get(idx).getValue());
                        configItemValue.setValue(configItem.getValues().get(idx).getValue());
                        idx++;
                    }
                }

                /*
                 * int index = configItem.getValueIndex(); if (configItem.getValues().size() == configItemValues.size()) { // equal size,
                 * overwrite int idx = 0; for (ConfigItemValueEntity configItemValue : configItemValues) {
                 * configItemValue.setValue(configItem.getValues().get(idx).getValue()); idx++; } } else { // not equal, clear and add
                 * this.configItemValueDAO.removeConfigItemValues(configItem); this.entityManager.flush(); List<ConfigItemValueEntity>
                 * copiedValues = new LinkedList<ConfigItemValueEntity>(configItem.getValues()); configItem =
                 * this.configItemDAO.findConfigItem(configGroup.getName(), configItem.getName()); for (ConfigItemValueEntity
                 * configItemValue : copiedValues) { LOG.debug("add item value: " + configItemValue.getValue());
                 * this.configItemValueDAO.addConfigItemValue(configItem, configItemValue.getValue()); } } configItem =
                 * this.configItemDAO.findConfigItem(configGroup.getName(), configItem.getName()); configItem.setValueIndex(index);
                 */
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ConfigItemValueEntity> listConfigItemValues(ConfigItemEntity configItem) {

        return this.configItemValueDAO.listConfigItemValues(configItem);
    }
}
