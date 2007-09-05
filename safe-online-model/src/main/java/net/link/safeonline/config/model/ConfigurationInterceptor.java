/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.entity.config.ConfigItemEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigurationInterceptor {

	private static final Log LOG = LogFactory
			.getLog(ConfigurationInterceptor.class);

	@EJB
	private ConfigItemDAO configItemDAO;

	@AroundInvoke
	public Object invoke(InvocationContext invocationContext) throws Exception {
		return configure(invocationContext);
	}

	@PostConstruct
	public void initializeConfiguration(InvocationContext invocationContext)
			throws Exception {
		LOG.debug("@PostConstruct configuration");
		configure(invocationContext);
	}

	private String getMethodName(InvocationContext invocationContext) {
		Method method = invocationContext.getMethod();
		if (null == method) {
			return "lifecycle callback method";
		}
		return method.getName();
	}

	private Object configure(InvocationContext invocationContext)
			throws Exception {
		Object target = invocationContext.getTarget();
		LOG.debug("Configuring: " + target.getClass().getName() + " (method: "
				+ getMethodName(invocationContext) + ")");

		try {

			Field[] fields = target.getClass().getDeclaredFields();
			for (Field field : fields) {
				Configurable configurable = field
						.getAnnotation(Configurable.class);
				if (null == configurable) {
					continue;
				}

				String name = configurable.name();
				if (name == null || name == "") {
					name = field.getName();
				}

				ConfigItemEntity configItem = configItemDAO
						.findConfigItem(name);
				if (configItem == null) {
					continue;
				}

				setValue(configItem, field, target);
			}
		} catch (Exception e) {
			throw new EJBException("Failed to configure bean", e);
		}
		return invocationContext.proceed();
	}

	private void setValue(ConfigItemEntity configItem, Field field,
			Object target) throws IllegalArgumentException,
			IllegalAccessException {
		Class<?> fieldType = field.getType();
		Object value;
		if (String.class.equals(fieldType)) {
			value = configItem.getValue();
		} else if (Integer.class.equals(fieldType)) {
			try {
				value = Integer.parseInt(configItem.getValue());
			} catch (NumberFormatException e) {
				LOG.error("invalid integer value for config item: "
						+ configItem.getName());
				/*
				 * In case the value is not OK, we continue and let the bean use
				 * its initial value as is.
				 */
				return;
			}
		} else if (Double.class.equals(fieldType)) {
			try {
				value = Double.parseDouble(configItem.getValue());
			} catch (NumberFormatException e) {
				LOG.error("invalid double value for config item: "
						+ configItem.getName());
				return;
			}
		} else if (Long.class.equals(fieldType)) {
			try {
				value = Long.parseLong(configItem.getValue());
			} catch (NumberFormatException e) {
				LOG.error("invalid long value for config item: "
						+ configItem.getName());
				return;
			}
		} else if (Boolean.class.equals(fieldType)) {
			value = Boolean.parseBoolean(configItem.getValue());
		} else {
			LOG.error("unsupported field type: " + fieldType.getName());
			return;
		}
		LOG
				.debug("Configuring field: " + field.getName() + "; value: "
						+ value);
		field.setAccessible(true);
		field.set(target, value);
	}
}
