package test.unit.net.link.safeonline.dao.bean;

import java.util.Random;

import net.link.safeonline.dao.bean.StatisticDAOBean;
import net.link.safeonline.dao.bean.StatisticDataPointDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class StatisticDataPointDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private StatisticDataPointDAOBean testedInstance;

	private StatisticDAOBean statisticDAO;

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
		this.testedInstance = new StatisticDataPointDAOBean();
		this.statisticDAO = new StatisticDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.statisticDAO, this.entityTestManager
				.getEntityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testStatisticDataPoint() {
		// setup
		Random generator = new Random();
		StatisticEntity statistic = this.statisticDAO.addStatistic("test",
				"domain", null);

		// operate
		this.testedInstance.addStatisticDataPoint("cat A", statistic, generator
				.nextInt(), 0, 0);
		this.testedInstance.addStatisticDataPoint("cat B", statistic, generator
				.nextInt(), 0, 0);
		this.testedInstance.cleanStatisticDataPoints(statistic);
		this.testedInstance.addStatisticDataPoint("cat A", statistic, generator
				.nextInt(), 0, 0);
		this.testedInstance.addStatisticDataPoint("cat B", statistic, generator
				.nextInt(), 0, 0);
	}
}
