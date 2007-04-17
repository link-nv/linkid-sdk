/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import javax.management.InstanceAlreadyExistsException;
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

	private JmxTestUtils() {
		// private
	}

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
	public static void setUp(String mbeanName)
			throws MalformedObjectNameException, NullPointerException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
		ObjectName mbeanObjectName = new ObjectName(mbeanName);
		Object testMBean = new DynamicTestMBean();
		mbeanServer.registerMBean(testMBean, mbeanObjectName);
	}
}
