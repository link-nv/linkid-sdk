/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import net.link.safeonline.config.service.ConfigurationService;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;
import net.link.safeonline.oper.Configuration;
import net.link.safeonline.oper.OperatorConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("configuration")
@LocalBinding(jndiBinding = Configuration.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ConfigurationBean implements Configuration {

    private static final Log        LOG = LogFactory.getLog(ConfigurationBean.class);

    @DataModel("configGroupList")
    private List<ConfigGroupEntity> configGroupList;

    @EJB(mappedName = ConfigurationService.JNDI_BINDING)
    private ConfigurationService    configurationService;

    @In(create = true)
    FacesMessages                   facesMessages;


    @Factory("configGroupList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void configGroupListFactory() {

        configGroupList = configurationService.listConfigGroups();
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        // empty
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save() {

        configurationService.saveConfiguration(configGroupList);
        return "saved";
    }

    @Factory("configItemValueList")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> configItemValueListFactory() {

        FacesContext context = FacesContext.getCurrentInstance();
        ConfigItemEntity configItem = (ConfigItemEntity) context.getExternalContext().getRequestMap().get("configItem");
        List<SelectItem> values = new LinkedList<SelectItem>();

        LOG.debug("list config item values for item " + configItem.getName());
        List<ConfigItemValueEntity> configItemValues = configurationService.listConfigItemValues(configItem);

        for (ConfigItemValueEntity value : configItemValues) {
            LOG.debug("add value: " + value.getValue());
            values.add(new SelectItem(value, value.getValue()));
        }

        return values;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void valueChanged(ValueChangeEvent event) {

        LOG.debug("value changed");

        ConfigItemValueEntity newValue = (ConfigItemValueEntity) event.getNewValue();
        LOG.debug("value changed: new value: " + newValue.toString());

        newValue.getConfigItem().setValueIndex(newValue.getConfigItem().getValues().indexOf(newValue));
    }

}
