/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private Map<String, DynamicTestMBean> dynamicTestMBeans;

    private List<ObjectName>              mbeanNames;

    private MBeanServer                   mbeanServer;


    public JmxTestUtils() {

        dynamicTestMBeans = new HashMap<String, DynamicTestMBean>();
        mbeanNames = new LinkedList<ObjectName>();
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
    public void setUp(String mbeanName)
            throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {

        if (null == mbeanServer) {
            mbeanServer = getMBeanServer();
        }
        ObjectName mbeanObjectName = new ObjectName(mbeanName);
        DynamicTestMBean dynamicTestMBean = new DynamicTestMBean();
        mbeanServer.registerMBean(dynamicTestMBean, mbeanObjectName);
        dynamicTestMBeans.put(mbeanName, dynamicTestMBean);
        mbeanNames.add(mbeanObjectName);
    }

    public void tearDown()
            throws InstanceNotFoundException, MBeanRegistrationException {

        for (ObjectName mbeanName : mbeanNames) {
            mbeanServer.unregisterMBean(mbeanName);
        }
    }

    @SuppressWarnings("unchecked")
    private MBeanServer getMBeanServer() {

        MBeanServer mbeanServerInstance = MBeanServerFactory.createMBeanServer("jboss");
        return mbeanServerInstance;
    }

    /**
     * Registers an action handler.
     * 
     * @param actionName
     * @param actionHandler
     */
    public void registerActionHandler(String mbeanName, String actionName, MBeanActionHandler actionHandler) {

        DynamicTestMBean dynamicTestMBean = dynamicTestMBeans.get(mbeanName);
        dynamicTestMBean.registerActionHandler(actionName, actionHandler);
    }
}
