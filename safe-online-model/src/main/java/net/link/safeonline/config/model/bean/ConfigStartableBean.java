/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model.bean;

import static net.link.safeonline.common.Configurable.defaultGroup;

import java.lang.reflect.Field;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.dao.ConfigGroupDAO;
import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.config.model.ConfigStartable;
import net.link.safeonline.config.model.ConfigurationDeploymentStrategy;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = ConfigStartable.JNDI_BINDING)
public class ConfigStartableBean implements ConfigStartable {

    private static final Log LOG = LogFactory.getLog(ConfigStartableBean.class);

    @EJB(mappedName = ConfigItemDAO.JNDI_BINDING)
    private ConfigItemDAO    configItemDAO;

    @EJB(mappedName = ConfigGroupDAO.JNDI_BINDING)
    private ConfigGroupDAO   configGroupDAO;


    public int getPriority() {

        return Startable.PRIORITY_BOOTSTRAP;
    }

    public void postStart() {

        LOG.debug("Starting configuration");

        ConfigurationDeploymentStrategy configurationDeploymentStrategy = new ConfigurationDeploymentStrategy();
        configurationDeploymentStrategy.scan();

        for (Class<?> classObject : configurationDeploymentStrategy.getScannedConfigurationClasses()) {
            LOG.debug("found configurable class: " + classObject.getName());
            configure(classObject);
        }
    }

    public void preStop() {

        // empty
    }

    @SuppressWarnings("unchecked")
    protected void configure(Class classObject) {

        try {
            Object target = classObject.newInstance();
            Field[] fields = classObject.getDeclaredFields();

            Configurable generalConfigurable = (Configurable) classObject.getAnnotation(Configurable.class);

            String group = generalConfigurable.group();

            LOG.debug("Configuring: " + classObject.getName());

            for (Field field : fields) {
                LOG.debug("Inspecting field: " + field.getName());
                Configurable configurable = field.getAnnotation(Configurable.class);
                if (configurable != null) {
                    LOG.debug("Configuring field: " + field.getName());
                    String name = configurable.name();
                    if (name == null || name == "") {
                        name = field.getName();
                    }

                    if (!configurable.group().equals(defaultGroup)) {
                        group = configurable.group();
                    }
                    ConfigGroupEntity configGroup = this.configGroupDAO.findConfigGroup(group);
                    if (configGroup == null) {
                        LOG.debug("Adding configuration group: " + group);
                        configGroup = this.configGroupDAO.addConfigGroup(group);
                    }

                    ConfigItemEntity configItem = this.configItemDAO.findConfigItem(name);
                    field.setAccessible(true);
                    if (configItem == null) {
                        LOG.debug("Adding configuration item: " + name);
                        Object value = field.get(target);
                        String valueType = value.getClass().getName();
                        String stringValue = value.toString();
                        configItem = this.configItemDAO.addConfigItem(name, stringValue, valueType, configGroup);
                    } else {
                        configItem.setConfigGroup(configGroup);
                    }
                }
            }
        } catch (Exception e) {
            throw new EJBException("Failed to execute @Configurable", e);
        }
    }

}
