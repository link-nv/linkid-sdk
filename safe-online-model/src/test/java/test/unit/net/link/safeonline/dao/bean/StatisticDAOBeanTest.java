/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.StatisticDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class StatisticDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private StatisticDAOBean testedInstance;

	private ApplicationDAOBean applicationDAO;

	private ApplicationOwnerDAOBean applicationOwnerDAO;

	private SubjectDAOBean subjectDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(StatisticEntity.class,
				StatisticDataPointEntity.class, ApplicationEntity.class,
				ApplicationOwnerEntity.class, SubjectEntity.class);
		// StatisticDataPointEntity.class,
		this.testedInstance = new StatisticDAOBean();
		this.applicationDAO = new ApplicationDAOBean();
		this.applicationOwnerDAO = new ApplicationOwnerDAOBean();
		this.subjectDAO = new SubjectDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.applicationDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.applicationOwnerDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.subjectDAO, this.entityTestManager
				.getEntityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testStatistic() {
		// setup
		SubjectEntity subject = this.subjectDAO.addSubject("testsubject");
		this.applicationOwnerDAO.addApplicationOwner("testowner", subject);
		ApplicationOwnerEntity owner = this.applicationOwnerDAO
				.findApplicationOwner("testowner");
		ApplicationEntity application = this.applicationDAO.addApplication(
				"testapp", null, owner, null, null);

		// operate
		StatisticEntity original = this.testedInstance.addStatistic("test",
				"domain", application);
		StatisticEntity result = this.testedInstance.findStatisticById(original
				.getId());
		assertEquals(original, result);
		result = this.testedInstance.findStatisticByNameDomainAndApplication(
				"test", "domain", application);
		assertEquals(original, result);

		original = this.testedInstance.addStatistic("test2", "domain", null);
		result = this.testedInstance.findStatisticByNameDomainAndApplication(
				"test2", "domain", null);
		assertEquals(original, result);

		this.testedInstance.cleanDomain("domain");
		result = this.testedInstance.findStatisticByNameDomainAndApplication(
				"test2", "domain", null);
		assertNull(result);

	}

}
