/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

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
		LOG.debug("invoked");
		return null;
	}

	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
	}

	public AttributeList setAttributes(AttributeList attributes) {
		return null;
	}
}
