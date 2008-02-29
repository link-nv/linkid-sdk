package test.integ.net.link.safeonline.performance;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.DriverProfileService;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.performance.service.bean.DriverExceptionServiceBean;
import net.link.safeonline.performance.service.bean.DriverProfileServiceBean;
import net.link.safeonline.performance.service.bean.ExecutionServiceBean;
import net.link.safeonline.performance.service.bean.ProfileDataServiceBean;
import net.link.safeonline.performance.service.bean.ProfilingServiceBean;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mbillemo
 *
 */
@SuppressWarnings("unused")
public class DataTest {

	private static final String DB_NAME = "safeonline";
	private static final String DB_USER = "safeonline";
	private static final String DB_PASS = "safeonline";
	private static final String DB_HOST = "sebeco-dev-11";
	private static final int DB_PORT = 3306;

	private static final Class<?>[] entities = new Class[] {
			ScenarioTimingEntity.class, ExecutionEntity.class,
			DriverProfileEntity.class, ProfileDataEntity.class,
			MeasurementEntity.class };

	private ExecutionService executionService = new ExecutionServiceBean();
	private ProfileDataService profileDataService = new ProfileDataServiceBean();
	private DriverExceptionService driverExceptionService = new DriverExceptionServiceBean();
	private DriverProfileService driverProfileService = new DriverProfileServiceBean();

	@Test
	public void sample() throws Exception {

		long end = 1204222728063l, period = 5l * 60 * 1000;
		Long count = (Long) this.em.createQuery(
				"SELECT COUNT(t)                      "
						+ "FROM ProfileDataEntity d "
						+ "JOIN d.scenarioTiming t "
						+ "WHERE t.startTime >= :base AND t.startTime < :end")
				.setParameter("base", end - period).setParameter("end", end)
				.getSingleResult();

		System.err.println("count: " + count);
	}

	@Test
	public void testData() throws Exception {

		Date startTime = new Date(1204215098000l);
		long baseSample = 0l, period = 5l * 60 * 1000;

		ExecutionEntity execution = this.executionService
				.getExecution(startTime);
		DriverProfileEntity profile = this.driverProfileService.getProfile(
				AuthDriver.NAME, execution);

		List<ProfileDataEntity> profileData = this.profileDataService
				.getProfileData(profile, 0);
		int count = 0;
		for (ProfileDataEntity data : profileData) {
			Long start = data.getScenarioTiming().getStart();
			if (start >= baseSample && start < baseSample + period)
				count++;
		}

		System.err.println("Found " + count + " data entities.");
	}

	/*
	 * --------------------------- INTERNAL ---------------------------
	 */

	private static final Log LOG = LogFactory.getLog(DataTest.class);

	private EntityTestManager entityTestManager;
	private EntityManager em;

	@Before
	public void setUp() {

		this.entityTestManager = new EntityTestManager();

		try {
			this.entityTestManager.configureMySql(DB_HOST, DB_PORT, DB_NAME,
					DB_USER, DB_PASS);
			this.entityTestManager.setUp(entities);

			this.em = this.entityTestManager.getEntityManager();
			ProfilingServiceBean.setDefaultEntityManager(this.em);
		}

		catch (Exception e) {
			LOG.fatal("JPA annotations incorrect: " + e.getMessage(), e);
			throw new RuntimeException("JPA annotations incorrect: "
					+ e.getMessage(), e);
		}
	}

	@After
	public void tearDown() throws Exception {

		if (this.entityTestManager.getEntityManager() != null)
			this.entityTestManager.tearDown();
	}

}
