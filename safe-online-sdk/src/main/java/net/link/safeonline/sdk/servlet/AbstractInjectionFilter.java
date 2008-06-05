/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.servlet;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import net.link.safeonline.sdk.servlet.annotation.Context;
import net.link.safeonline.sdk.servlet.annotation.Init;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract Injection Filter.
 * 
 * <ul>
 * <li>Injects EJBs.
 * <li>Injects filter init parameters. If no defaultValue is specified, an
 * {@link UnavailableException} will be thrown.
 * <li>Injects filter context parameters. If no defaultValue is specified, an
 * {@link UnavailableException} will be thrown.
 * </ul>
 * 
 * @author wvdhaute
 * 
 */
public abstract class AbstractInjectionFilter implements Filter {

	private static final Log LOG = LogFactory
			.getLog(AbstractInjectionFilter.class);

	private static final long serialVersionUID = 1L;

	protected Map<String, String> configParams;

	public void init(FilterConfig config) throws ServletException {
		initInitParameters(config);
		initContextParameters(config);
		injectEjbs();
	}

	@SuppressWarnings("unchecked")
	private void injectEjbs() throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			EJB ejb = field.getAnnotation(EJB.class);
			if (null == ejb) {
				continue;
			}
			String mappedName = ejb.mappedName();
			if (null == mappedName) {
				throw new ServletException("@EJB mappedName attribute required");
			}
			LOG.debug("injecting: " + mappedName);
			Class type = field.getType();
			if (false == type.isInterface()) {
				throw new ServletException("field is not an interface type");
			}
			Object ejbRef = EjbUtils.getEJB(mappedName, type);
			field.setAccessible(true);
			try {
				field.set(this, ejbRef);
			} catch (IllegalArgumentException e) {
				throw new ServletException("illegal argument: "
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new ServletException("illegal access: " + e.getMessage(),
						e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initInitParameters(FilterConfig config)
			throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Init initAnnotation = field.getAnnotation(Init.class);
			if (null == initAnnotation) {
				continue;
			}
			String name = initAnnotation.name();
			if (null == name) {
				throw new ServletException("@Init name attribute required");
			}
			LOG.debug("init: " + name);
			String defaultValue = initAnnotation.defaultValue();
			boolean optional = initAnnotation.optional();

			String value = config.getInitParameter(name);
			if (null == value) {
				if (Init.NOT_SPECIFIED.equals(defaultValue) && !optional) {
					throw new UnavailableException("missing init parameter: "
							+ name);
				}
				if (Init.NOT_SPECIFIED.equals(defaultValue)) {
					defaultValue = null;
				}
				value = defaultValue;
			}
			field.setAccessible(true);
			try {
				field.set(this, value);
			} catch (IllegalArgumentException e) {
				throw new ServletException("illegal argument: "
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new ServletException("illegal access: " + e.getMessage(),
						e);
			}
		}
		this.configParams = new HashMap<String, String>();
		Enumeration<String> initParamsEnum = config.getInitParameterNames();
		while (initParamsEnum.hasMoreElements()) {
			String paramName = initParamsEnum.nextElement();
			String paramValue = config.getInitParameter(paramName);
			LOG.debug("config param: " + paramName + "=" + paramValue);
			this.configParams.put(paramName, paramValue);
		}
	}

	private void initContextParameters(FilterConfig config)
			throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Context contextAnnotation = field.getAnnotation(Context.class);
			if (null == contextAnnotation) {
				continue;
			}
			String name = contextAnnotation.name();
			if (null == name) {
				throw new ServletException("@Context name attribute required");
			}
			LOG.debug("init: " + name);
			String defaultValue = contextAnnotation.defaultValue();
			boolean optional = contextAnnotation.optional();

			String value = config.getServletContext().getInitParameter(name);
			if (null == value) {
				if (Context.NOT_SPECIFIED.equals(defaultValue) && !optional) {
					throw new UnavailableException("missing init parameter: "
							+ name);
				}
				if (Context.NOT_SPECIFIED.equals(defaultValue)) {
					defaultValue = null;
				}
				value = defaultValue;
			}
			field.setAccessible(true);
			try {
				field.set(this, value);
			} catch (IllegalArgumentException e) {
				throw new ServletException("illegal argument: "
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new ServletException("illegal access: " + e.getMessage(),
						e);
			}
		}
	}
}
