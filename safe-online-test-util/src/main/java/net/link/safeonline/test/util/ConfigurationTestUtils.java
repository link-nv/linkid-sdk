package net.link.safeonline.test.util;

import java.lang.reflect.Field;

import net.link.safeonline.common.Configurable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigurationTestUtils {

	private static final Log LOG = LogFactory
			.getLog(ConfigurationTestUtils.class);

	public static void configure(Object target, String name, String value)
			throws Exception {
		Field[] fields = target.getClass().getDeclaredFields();

		for (Field field : fields) {
			Configurable configurable = field.getAnnotation(Configurable.class);
			if (configurable != null) {
				field.setAccessible(true);
				if (configurable.name().equals(name)
						|| (configurable.name().equals("") && field.getName()
								.equals(name))) {
					LOG.debug("setting field: " + name + " to: " + value);
					field.set(target, value);
				}
			}
		}

	}

}
