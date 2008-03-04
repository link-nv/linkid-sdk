package test.integ.net.link.safeonline.performance;

import javax.persistence.EntityManager;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.DriverProfileService;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.performance.service.ScenarioTimingService;
import net.link.safeonline.performance.service.bean.DriverExceptionServiceBean;
import net.link.safeonline.performance.service.bean.DriverProfileServiceBean;
import net.link.safeonline.performance.service.bean.ExecutionServiceBean;
import net.link.safeonline.performance.service.bean.ProfileDataServiceBean;
import net.link.safeonline.performance.service.bean.ProfilingServiceBean;
import net.link.safeonline.performance.service.bean.ScenarioTimingServiceBean;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

/**
 * @author mbillemo
 *
 */
@SuppressWarnings("unused")
public abstract class AbstractDataTest {

	protected final Log LOG = LogFactory.getLog(getClass());

	protected final String DB_NAME = "safeonline";
	protected final String DB_USER = "safeonline";
	protected final String DB_PASS = "safeonline";
	protected final String DB_HOST = "sebeco-dev-11";
	protected final int DB_PORT = 3306;

	protected final Class<?>[] entities = new Class[] {
			ScenarioTimingEntity.class, ExecutionEntity.class,
			DriverProfileEntity.class, ProfileDataEntity.class,
			MeasurementEntity.class };

	protected ExecutionService executionService;
	protected ProfileDataService profileDataService;
	protected DriverExceptionService driverExceptionService;
	protected DriverProfileService driverProfileService;
	protected ScenarioTimingService scenarioTimingService;

	protected EntityManager em;
	private EntityTestManager entityTestManager;

	@Before
	public void setUp() {

		this.entityTestManager = new EntityTestManager();

		try {
			this.entityTestManager.configureMySql(this.DB_HOST, this.DB_PORT, this.DB_NAME,
					this.DB_USER, this.DB_PASS);
			this.entityTestManager.setUp(this.entities);

			this.em = this.entityTestManager.getEntityManager();
			ProfilingServiceBean.setDefaultEntityManager(this.em);

			this.executionService = new ExecutionServiceBean();
			this.profileDataService = new ProfileDataServiceBean();
			this.driverExceptionService = new DriverExceptionServiceBean();
			this.driverProfileService = new DriverProfileServiceBean();
			this.scenarioTimingService = new ScenarioTimingServiceBean();
		}

		catch (Exception e) {
			this.LOG.fatal("JPA annotations incorrect: " + e.getMessage(), e);
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
