package net.link.safeonline.model;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.common.Configurable;
import net.link.safeonline.dao.ConfigItemDAO;
import net.link.safeonline.entity.config.ConfigItemEntity;

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
	public void initializeConfiguration(InvocationContext invocationContext) {
		LOG.debug("postconstruct configuration: "
				+ invocationContext.getTarget().getClass().getName());
		configure(invocationContext);
	}

	private Object configure(InvocationContext invocationContext) {
		try {
			Object target = invocationContext.getTarget();
			Field[] fields = target.getClass().getDeclaredFields();

			LOG.debug("Configuring: " + target.getClass().getName());

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

					ConfigItemEntity configItem = configItemDAO
							.findConfigItem(name);
					field.setAccessible(true);
					if (configItem == null) {
						continue;
					}

					field.set(target, configItem.getValue());

					return invocationContext.proceed();
				}
			}
		} catch (Exception e) {
			throw new EJBException("Failed to configure bean", e);
		}
		return null;
	}
}
