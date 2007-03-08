/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import java.util.ArrayList;
import java.util.List;

import net.link.safeonline.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.service.bean.ConfigurationServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class ConfigurationServiceBeanTest extends TestCase {

	private ConfigurationServiceBean testedInstance;

	private EntityTestManager entityTestManager;

	private ConfigItemDAOBean configItemDAO;

	private ConfigGroupDAOBean configGroupDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new ConfigurationServiceBean();
		this.configGroupDAO = new ConfigGroupDAOBean();
		this.configItemDAO = new ConfigItemDAOBean();

		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(ConfigGroupEntity.class,
				ConfigItemEntity.class);

		EJBTestUtils.inject(this.configGroupDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.configItemDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.testedInstance, this.configItemDAO);
		EJBTestUtils.inject(this.testedInstance, this.configGroupDAO);
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testSaveConfigGroup() {
		// setup
		ConfigGroupEntity configGroup = this.configGroupDAO
				.addConfigGroup("group 1");
		this.configItemDAO.addConfigItem("item 1", "value 1", configGroup);

		ConfigGroupEntity detachedGroup = new ConfigGroupEntity("group 1");
		ConfigItemEntity detachedItem = new ConfigItemEntity("item 1",
				"value 2", detachedGroup);
		detachedGroup.getConfigItems().add(detachedItem);
		List<ConfigGroupEntity> groupList = new ArrayList<ConfigGroupEntity>();
		groupList.add(detachedGroup);

		// operate
		this.testedInstance.saveConfiguration(groupList);

		// verify
		groupList = this.testedInstance.getConfigGroups();
		String result = groupList.get(0).getConfigItems().get(0).getValue();
		assertEquals(result, "value 2");
	}

}
