/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dummy MBean for unit testing purposes.
 * 
 * @author fcorneli
 * 
 */
public class DynamicTestMBean implements DynamicMBean {

	private static final Log LOG = LogFactory.getLog(DynamicTestMBean.class);

	private Map<String, MBeanActionHandler> actionHandlers = new HashMap<String, MBeanActionHandler>();

	/**
	 * Registers an action handler that this dynamic test MBean will be using
	 * when invoking actions.
	 * 
	 * @param actionName
	 * @param action
	 */
	public void registerActionHandler(String actionName,
			MBeanActionHandler action) {
		if (this.actionHandlers.containsKey(actionName)) {
			throw new IllegalStateException("already registered mbean action: "
					+ actionName);
		}
		this.actionHandlers.put(actionName, action);
	}

	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		return null;
	}

	public AttributeList getAttributes(String[] attributes) {
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		return new MBeanInfo(this.getClass().getName(), "test", null, null,
				null, null);
	}

	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		LOG.debug("invoked: " + actionName);
		MBeanActionHandler actionHandler = this.actionHandlers.get(actionName);
		if (null == actionHandler) {
			return null;
		}
		Object result = actionHandler.invoke(params);
		return result;
	}

	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
	}

	public AttributeList setAttributes(AttributeList attributes) {
		return null;
	}
}
