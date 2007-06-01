/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util.ri;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBException;

import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;

/**
 * Implementation class for injection JAX-WS RI instance resolver. This JAX-WS
 * RI instance resolver injects JNDI components. Simply use the EJB annotation
 * with mappedName attribute on the injection fields of your JAX-WS endpoints.
 * Use only to inject stateless session beans. This cannot be used for injection
 * of stateful session beans.
 * 
 * @author fcorneli
 * 
 * @param <T>
 */
public class InjectionInstanceResolver<T> extends
		AbstractMultiInstanceResolver<T> {

	private static final Log LOG = LogFactory
			.getLog(InjectionInstanceResolver.class);

	private static final Map<Class, Object> instances = new Hashtable<Class, Object>();

	private final Class<T> clazz;

	public InjectionInstanceResolver(Class<T> clazz) {
		super(clazz);
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T resolve(Packet request) {
		T instance = (T) instances.get(this.clazz);
		if (null == instance) {
			LOG.debug("creating new instance for class: "
					+ this.clazz.getName());
			instance = create();
			ejbInjection(instance);
			instances.put(this.clazz, instance);
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	private void ejbInjection(T instance) {
		Field[] fields = this.clazz.getDeclaredFields();
		for (Field field : fields) {
			EJB ejb = field.getAnnotation(EJB.class);
			if (null == ejb) {
				continue;
			}
			String mappedName = ejb.mappedName();
			if (null == mappedName) {
				throw new EJBException("@EJB mappedName attribute required");
			}
			LOG.debug("injecting: " + mappedName);
			Class type = field.getType();
			if (false == type.isInterface()) {
				throw new EJBException("field is not an interface type");
			}
			Object ejbRef = EjbUtils.getEJB(mappedName, type);
			field.setAccessible(true);
			try {
				field.set(instance, ejbRef);
			} catch (IllegalArgumentException e) {
				throw new EJBException("illegal argument");
			} catch (IllegalAccessException e) {
				throw new EJBException("illegal access");
			}
		}
	}
}
