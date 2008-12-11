/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.config.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.config.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.config.dao.bean.ConfigItemValueDAOBean;
import net.link.safeonline.config.service.bean.ConfigurationServiceBean;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;


public class ConfigurationServiceBeanTest extends TestCase {

    private ConfigurationServiceBean testedInstance;

    private EntityTestManager        entityTestManager;

    private ConfigItemDAOBean        configItemDAO;

    private ConfigGroupDAOBean       configGroupDAO;

    private ConfigItemValueDAOBean   configItemValueDAO;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        this.testedInstance = new ConfigurationServiceBean();
        this.configGroupDAO = new ConfigGroupDAOBean();
        this.configItemDAO = new ConfigItemDAOBean();
        this.configItemValueDAO = new ConfigItemValueDAOBean();

        this.entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        this.entityTestManager.setUp(ConfigGroupEntity.class, ConfigItemEntity.class, ConfigItemValueEntity.class);

        EJBTestUtils.inject(this.configGroupDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.configItemDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.configItemValueDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.testedInstance, this.configItemValueDAO);
        EJBTestUtils.inject(this.testedInstance, this.configItemDAO);
        EJBTestUtils.inject(this.testedInstance, this.configGroupDAO);

        EJBTestUtils.init(this.configItemValueDAO);
        EJBTestUtils.init(this.configItemDAO);
        EJBTestUtils.init(this.configGroupDAO);
    }

    @Override
    protected void tearDown()
            throws Exception {

        this.entityTestManager.tearDown();
        super.tearDown();
    }

    public void testSaveConfigGroup() {

        // setup
        ConfigGroupEntity configGroup = this.configGroupDAO.addConfigGroup("group 1");
        ConfigItemEntity configItem = this.configItemDAO.addConfigItem("item 1", String.class.getName(), false, configGroup);
        this.configItemValueDAO.addConfigItemValue(configItem, "value 1");

        ConfigGroupEntity detachedGroup = new ConfigGroupEntity("group 1");
        ConfigItemEntity detachedItem = new ConfigItemEntity("item 1", String.class.getName(), false, detachedGroup);
        ConfigItemValueEntity detachedItemValue = new ConfigItemValueEntity(detachedItem, "value 2");
        detachedGroup.getConfigItems().add(detachedItem);
        detachedItem.getValues().add(detachedItemValue);
        List<ConfigGroupEntity> groupList = new ArrayList<ConfigGroupEntity>();
        groupList.add(detachedGroup);

        // operate
        this.testedInstance.saveConfiguration(groupList);

        // verify
        groupList = this.testedInstance.listConfigGroups();
        Iterator<ConfigItemEntity> it = groupList.get(0).getConfigItems().iterator();
        assertTrue(it.hasNext());
        ConfigItemEntity resultItem = it.next();
        String result = resultItem.getValue();
        assertEquals(1, resultItem.getValues().size());
        assertEquals("value 2", result);
    }

    public void testSaveConfigGroupMultipleChoice() {

        // setup
        ConfigGroupEntity configGroup = this.configGroupDAO.addConfigGroup("group 1");
        ConfigItemEntity configItem = this.configItemDAO.addConfigItem("item 1", String.class.getName(), true, configGroup);
        configItem.setValueIndex(1);
        this.configItemValueDAO.addConfigItemValue(configItem, "value 1");
        this.configItemValueDAO.addConfigItemValue(configItem, "value 2");
        ConfigItemValueEntity itemValue3 = this.configItemValueDAO.addConfigItemValue(configItem, "value 3");

        ConfigGroupEntity detachedGroup = new ConfigGroupEntity("group 1");
        ConfigItemEntity detachedItem = new ConfigItemEntity("item 1", String.class.getName(), true, detachedGroup);
        ConfigItemValueEntity detachedItemValue1 = new ConfigItemValueEntity(detachedItem, "value 1");
        ConfigItemValueEntity detachedItemValue2 = new ConfigItemValueEntity(detachedItem, "value 2");
        ConfigItemValueEntity detachedItemValue3 = new ConfigItemValueEntity(detachedItem, "value 3");
        detachedItemValue3.setId(itemValue3.getId());
        detachedGroup.getConfigItems().add(detachedItem);
        detachedItem.getValues().add(detachedItemValue1);
        detachedItem.getValues().add(detachedItemValue2);
        detachedItem.getValues().add(detachedItemValue3);
        detachedItem.setValue(detachedItemValue3.getValue());
        List<ConfigGroupEntity> groupList = new ArrayList<ConfigGroupEntity>();
        groupList.add(detachedGroup);

        // operate
        this.testedInstance.saveConfiguration(groupList);

        // verify
        groupList = this.testedInstance.listConfigGroups();
        Iterator<ConfigItemEntity> it = groupList.get(0).getConfigItems().iterator();
        assertTrue(it.hasNext());
        ConfigItemEntity resultItem = it.next();

        assertEquals(3, resultItem.getValues().size());
        String result = resultItem.getValue();
        assertEquals(2, resultItem.getValueIndex());
        assertEquals("value 3", result);
    }
}
