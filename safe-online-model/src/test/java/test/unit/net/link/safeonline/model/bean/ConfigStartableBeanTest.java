/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import junit.framework.TestCase;
import net.link.safeonline.ConfigurationProvider;
import net.link.safeonline.Startable;
import net.link.safeonline.model.ConfigStartable;
import net.link.safeonline.model.bean.ConfigStartableBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class ConfigStartableBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private ConfigStartable testedInstance;

	private JndiTestUtils jndiTestUtils;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);

		systemStartable.postStart();
		entityManager = this.entityTestManager.refreshEntityManager();

		this.testedInstance = EJBTestUtils.newInstance(
				ConfigStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		this.jndiTestUtils.tearDown();
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testPostStart() throws Exception {
		// setup
		ConfigurationProvider testConfigComponent = new TestConfigurationProvider();
		this.jndiTestUtils.bindComponent(
				"SafeOnline/config/TestConfigComponent", testConfigComponent);

		// operate
		this.testedInstance.postStart();
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.commit();
		entityTransaction.begin();
		/*
		 * run the postStart twice since the system must be capable of rebooting
		 * using a non-volatile database.
		 */
		this.testedInstance.postStart();
	}

	private static class TestConfigurationProvider implements
			ConfigurationProvider {

		public Map<String, String> getConfigurationParameters() {
			Map<String, String> testConfigurationParameters = new TreeMap<String, String>();
			testConfigurationParameters.put("test-param-name",
					"test-default-value");
			return testConfigurationParameters;
		}

		public String getGroupName() {
			return "test-group-name";
		}
	}
}
