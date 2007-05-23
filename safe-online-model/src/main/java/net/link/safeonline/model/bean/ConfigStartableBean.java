/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.lang.reflect.Field;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.Startable;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.dao.ConfigGroupDAO;
import net.link.safeonline.dao.ConfigItemDAO;
import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.model.ConfigStartable;
import net.link.safeonline.model.ConfigurableScanner;

import static net.link.safeonline.common.Configurable.defaultGroup;

@Stateless
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "ConfigStartableBean")
public class ConfigStartableBean implements ConfigStartable {

	private static final Log LOG = LogFactory.getLog(ConfigStartableBean.class);

	@EJB
	private ConfigItemDAO configItemDAO;

	@EJB
	private ConfigGroupDAO configGroupDAO;

	public int getPriority() {
		return Startable.PRIORITY_BOOTSTRAP;
	}

	public void postStart() {
		LOG.debug("Starting configuration");
		ConfigurableScanner scanner = new ConfigurableScanner(
				"config.properties");
		for (Class classObject : scanner.getClasses()) {
			LOG.debug("found configurable class: " + classObject.getName());
			configure(classObject);
		}
	}

	public void preStop() {
		// empty

	}

	@SuppressWarnings("unchecked")
	private void configure(Class classObject) {
		try {
			Object target = classObject.newInstance();
			Field[] fields = classObject.getDeclaredFields();

			Configurable generalConfigurable = (Configurable) classObject
					.getAnnotation(Configurable.class);

			String group = generalConfigurable.group();

			LOG.debug("Configuring: " + classObject.getName());

			for (Field field : fields) {
				LOG.debug("Inspecting field: " + field.getName());
				Configurable configurable = field
						.getAnnotation(Configurable.class);
				if (configurable != null) {
					LOG.debug("Configuring field: " + field.getName());
					String name = configurable.name();
					if (name == null || name == "") {
						name = field.getName();
					}

					if (!configurable.group().equals(defaultGroup)) {
						group = configurable.group();
					}
					ConfigGroupEntity configGroup = configGroupDAO
							.findConfigGroup(group);
					if (configGroup == null) {
						LOG.debug("Adding configuration group: " + group);
						configGroup = configGroupDAO.addConfigGroup(group);
					}

					ConfigItemEntity configItem = configItemDAO
							.findConfigItem(name);
					field.setAccessible(true);
					if (configItem == null) {
						LOG.debug("Adding configuration item: " + name);
						configItem = configItemDAO.addConfigItem(name,
								(String) field.get(target), configGroup);
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