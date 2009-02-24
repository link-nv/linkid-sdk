/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.config.model.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collections;

import junit.framework.TestCase;
import net.link.safeonline.config.dao.ConfigGroupDAO;
import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.config.dao.ConfigItemValueDAO;
import net.link.safeonline.config.model.bean.ConfigurationManagerBean;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;
import net.link.safeonline.test.util.EJBTestUtils;


public class ConfigurationManagerBeanTest extends TestCase {

    private ConfigurationManagerBean testedInstance;

    private ConfigGroupDAO           mockConfigGroupDAO;

    private ConfigItemDAO            mockConfigItemDAO;

    private ConfigItemValueDAO       mockConfigItemValueDAO;

    private Object[]                 mockObjects;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        testedInstance = new ConfigurationManagerBean();

        mockConfigGroupDAO = createMock(ConfigGroupDAO.class);
        EJBTestUtils.inject(testedInstance, mockConfigGroupDAO);

        mockConfigItemDAO = createMock(ConfigItemDAO.class);
        EJBTestUtils.inject(testedInstance, mockConfigItemDAO);

        mockConfigItemValueDAO = createMock(ConfigItemValueDAO.class);
        EJBTestUtils.inject(testedInstance, mockConfigItemValueDAO);

        mockObjects = new Object[] { mockConfigGroupDAO, mockConfigItemDAO, mockConfigItemValueDAO };

        EJBTestUtils.init(testedInstance);
    }

    public void testAddConfigurationValue()
            throws Exception {

        // setup
        String group = "test-config-group";
        String name = "test-config-name";
        String value = "test-config-value";

        ConfigGroupEntity configGroup = new ConfigGroupEntity(group);
        ConfigItemEntity configItem = new ConfigItemEntity(name, value.getClass().getName(), true, configGroup);
        ConfigItemValueEntity configItemValue = new ConfigItemValueEntity(configItem, value);

        // expectations
        expect(mockConfigGroupDAO.findConfigGroup(group)).andReturn(configGroup);
        expect(mockConfigItemDAO.findConfigItem(group, name)).andReturn(configItem);
        expect(mockConfigItemValueDAO.listConfigItemValues(configItem)).andReturn(null);
        expect(mockConfigItemValueDAO.addConfigItemValue(configItem, value)).andReturn(configItemValue);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.addConfigurationValue(group, name, true, value);

        // verify
        verify(mockObjects);
    }

    public void testAddConfigurationValueTwice()
            throws Exception {

        // setup
        String group = "test-config-group";
        String name = "test-config-name";
        String value = "test-config-value";

        ConfigGroupEntity configGroup = new ConfigGroupEntity(group);
        ConfigItemEntity configItem = new ConfigItemEntity(name, value.getClass().getName(), true, configGroup);
        ConfigItemValueEntity configItemValue = new ConfigItemValueEntity(configItem, value);

        // expectations
        expect(mockConfigGroupDAO.findConfigGroup(group)).andReturn(configGroup);
        expect(mockConfigItemDAO.findConfigItem(group, name)).andReturn(configItem);
        expect(mockConfigItemValueDAO.listConfigItemValues(configItem)).andReturn(Collections.singletonList(configItemValue));

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.addConfigurationValue(group, name, true, value);

        // verify
        verify(mockObjects);
    }
}
