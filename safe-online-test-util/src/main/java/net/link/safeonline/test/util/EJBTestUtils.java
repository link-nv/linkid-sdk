/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Util class for EJB3 unit testing.
 * 
 * @author fcorneli
 * 
 */
public final class EJBTestUtils {

	private static final Log LOG = LogFactory.getLog(EJBTestUtils.class);

	private EJBTestUtils() {
		// empty
	}

	/**
	 * Injects a value into a given object.
	 * 
	 * @param fieldName
	 *            the name of the field to set.
	 * @param object
	 *            the object on which to inject the value.
	 * @param value
	 *            the value to inject.
	 * @throws Exception
	 */
	public static void inject(String fieldName, Object object, Object value)
			throws Exception {
		if (null == fieldName) {
			throw new IllegalArgumentException("field name should not be null");
		}
		if (null == value) {
			throw new IllegalArgumentException("the value should not be null");
		}
		if (null == object) {
			throw new IllegalArgumentException("the object should not be null");
		}
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(object, value);
	}

	/**
	 * Injects a resource value into an object.
	 * 
	 * @param bean
	 *            the bean object on which to inject a value.
	 * @param resourceName
	 *            the source name.
	 * @param value
	 *            the value to inject.
	 */
	public static void injectResource(Object bean, String resourceName,
			Object value) throws Exception {
		if (null == bean) {
			throw new IllegalArgumentException(
					"the bean object should not be null");
		}
		if (null == resourceName) {
			throw new IllegalArgumentException(
					"the resource name should not be null");
		}
		if (null == value) {
			throw new IllegalArgumentException(
					"the value object should not be null");
		}
		Class beanClass = bean.getClass();
		Field[] fields = beanClass.getDeclaredFields();
		for (Field field : fields) {
			Resource resourceAnnotation = field.getAnnotation(Resource.class);
			if (null == resourceAnnotation) {
				continue;
			}
			if (!resourceName.equals(resourceAnnotation.name())) {
				continue;
			}
			field.setAccessible(true);
			field.set(bean, value);
			return;
		}
		throw new IllegalArgumentException("resource field not found");
	}

	/**
	 * Injects a value object into a given bean object.
	 * 
	 * @param object
	 *            the bean object in which to inject.
	 * @param value
	 *            the value object to inject.
	 * @throws Exception
	 */
	public static void inject(Object object, Object value) throws Exception {
		if (null == value) {
			throw new IllegalArgumentException("the value should not be null");
		}
		if (null == object) {
			throw new IllegalArgumentException("the object should not be null");
		}
		Field[] fields = object.getClass().getDeclaredFields();
		Field selectedField = null;
		for (Field field : fields) {
			if (field.getType().isInstance(value)) {
				if (null != selectedField) {
					throw new IllegalStateException(
							"two field found of same injection type");
				}
				selectedField = field;
			}
		}
		if (null == selectedField) {
			throw new IllegalStateException("field of injection type not found");
		}
		selectedField.setAccessible(true);
		selectedField.set(object, value);
	}

	/**
	 * Initializes a bean.
	 * 
	 * @param bean
	 *            the bean to initialize.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static void init(Object bean) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		LOG.debug("Initializing: " + bean);
		Class clazz = bean.getClass();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			PostConstruct postConstruct = method
					.getAnnotation(PostConstruct.class);
			if (null == postConstruct) {
				continue;
			}
			method.invoke(bean, new Object[] {});
		}
	}

	@SuppressWarnings("unchecked")
	public static <Type> Type newInstance(Class<Type> clazz, Class[] container,
			EntityManager entityManager) {
		Type instance;
		try {
			instance = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("instantiation error");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("illegal access error");
		}
		InvocationHandler transactionInvocationHandler = new TestContainerInvocationHandler(
				instance, container, entityManager);
		Class[] interfaces = clazz.getInterfaces();
		if (0 == interfaces.length) {
			interfaces = clazz.getSuperclass().getInterfaces();
		}
		Type proxy = (Type) Proxy.newProxyInstance(clazz.getClassLoader(),
				interfaces, transactionInvocationHandler);
		return proxy;
	}

	/**
	 * Test EJB3 Container invocation handler. Be careful here not to start
	 * writing an entire EJB3 container.
	 * 
	 * @author fcorneli
	 * 
	 */
	private static class TestContainerInvocationHandler implements
			InvocationHandler {

		private static final Log LOG = LogFactory
				.getLog(TestContainerInvocationHandler.class);

		private final Object object;

		private final Class[] container;

		private final EntityManager entityManager;

		public TestContainerInvocationHandler(Object object, Class[] container,
				EntityManager entityManager) {
			this.object = object;
			this.container = container;
			this.entityManager = entityManager;
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			checkSessionBean();
			Class clazz = this.object.getClass();
			Class superClass = clazz.getSuperclass();
			injectDependencies(clazz);
			injectDependencies(superClass);
			injectEntityManager(clazz);
			injectEntityManager(superClass);
			try {
				Object result = method.invoke(this.object, args);
				return result;
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

		@SuppressWarnings("unchecked")
		private void checkSessionBean() {
			Class clazz = this.object.getClass();
			Stateless statelessAnnotation = (Stateless) clazz
					.getAnnotation(Stateless.class);
			if (null == statelessAnnotation) {
				throw new EJBException("no @Stateless annotation found");
			}
		}

		@SuppressWarnings("unchecked")
		private void injectDependencies(Class clazz) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				EJB ejbAnnotation = field.getAnnotation(EJB.class);
				if (null == ejbAnnotation) {
					continue;
				}
				Class fieldType = field.getType();
				if (false == fieldType.isInterface()) {
					throw new EJBException("field is not an interface type");
				}
				Local localAnnotation = (Local) fieldType
						.getAnnotation(Local.class);
				if (null == localAnnotation) {
					throw new EJBException(
							"interface has no @Local annotation: "
									+ fieldType.getName());
				}
				Class beanType = getBeanType(fieldType);
				Object bean = EJBTestUtils.newInstance(beanType,
						this.container, this.entityManager);
				setField(field, bean);
			}
		}

		private void setField(Field field, Object value) {
			field.setAccessible(true);
			try {
				LOG.debug("injecting " + value + " into " + this.object);
				field.set(this.object, value);
			} catch (IllegalArgumentException e) {
				throw new EJBException("illegal argument error");
			} catch (IllegalAccessException e) {
				throw new EJBException("illegal access error");
			}
		}

		private void injectEntityManager(Class clazz) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				PersistenceContext persistenceContextAnnotation = field
						.getAnnotation(PersistenceContext.class);
				if (null == persistenceContextAnnotation) {
					continue;
				}
				Class fieldType = field.getType();
				if (false == EntityManager.class.isAssignableFrom(fieldType)) {
					throw new EJBException("field type not correct");
				}
				setField(field, this.entityManager);
			}
		}

		@SuppressWarnings("unchecked")
		private Class getBeanType(Class interfaceType) {
			for (Class containerClass : this.container) {
				if (false == interfaceType.isAssignableFrom(containerClass)) {
					continue;
				}
				return containerClass;
			}
			throw new EJBException("did not find a container class for type: "
					+ interfaceType.getName());
		}
	}
}
