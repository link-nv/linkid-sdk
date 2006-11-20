package net.link.safeonline.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;

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
		Stateless stateless = (Stateless) clazz.getAnnotation(Stateless.class);
		if (null == stateless) {
			LOG.warn("no Stateless annotation found on class");
		}
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
}
