package net.link.safeonline.model.bean;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.model.IdGenerator;

@Stateless
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class IdGeneratorBean implements IdGenerator {

	public String generateId() {
		return UUID.randomUUID().toString();
	}

}
