/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Utility methods for JMX unit testing.
 * 
 * @author fcorneli
 * 
 */
public class JmxTestUtils {

	private DynamicTestMBean dynamicTestMBean;

	private ObjectName mbeanName;

	/**
	 * Sets up a test JMX MBean with the given MBean name.
	 * 
	 * @param mbeanName
	 * @throws MalformedObjectNameException
	 * @throws NullPointerException
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws NotCompliantMBeanException
	 */
	public void setUp(String mbeanName) throws MalformedObjectNameException,
			NullPointerException, InstanceAlreadyExistsException,
			MBeanRegistrationException, NotCompliantMBeanException {
		MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
		ObjectName mbeanObjectName = new ObjectName(mbeanName);
		this.dynamicTestMBean = new DynamicTestMBean();
		mbeanServer.registerMBean(this.dynamicTestMBean, mbeanObjectName);
		this.mbeanName = mbeanObjectName;
	}

	public void tearDown() throws InstanceNotFoundException,
			MBeanRegistrationException {
		MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
		mbeanServer.unregisterMBean(this.mbeanName);
	}

	/**
	 * Registers an action handler.
	 * 
	 * @param actionName
	 * @param actionHandler
	 */
	public void registerActionHandler(String actionName,
			MBeanActionHandler actionHandler) {
		this.dynamicTestMBean.registerActionHandler(actionName, actionHandler);
	}
}
