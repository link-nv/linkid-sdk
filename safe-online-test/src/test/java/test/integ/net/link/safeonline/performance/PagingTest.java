package test.integ.net.link.safeonline.performance;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
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
public class PagingTest {

	static final Log LOG = LogFactory.getLog(PagingTest.class);

	private EntityTestManager entityTestManager;

	@Before
	public void setUp() {

		this.entityTestManager = new EntityTestManager();

		try {
			this.entityTestManager.configureMySql("sebeco-dev-11", 3306,
					"safeonline", "safeonline", "safeonline");
			this.entityTestManager.setUp(AgentTimeEntity.class,
					ExecutionEntity.class, DriverProfileEntity.class,
					ProfileDataEntity.class, MeasurementEntity.class);
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

	@Test
	public void annotationCorrectness() throws Exception {

		assertNotNull("JPA annotations incorrect?", this.entityTestManager
				.getEntityManager());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testPaging() throws Exception {

		EntityManager em = this.entityTestManager.getEntityManager();
		String driverName = "Authentication Driver";
		Date startTime = new Date(1203519790000l);

		// Find the driver profile.
		// DriverProfileEntity profile = (DriverProfileEntity) em
		// .createQuery(
		// "SELECT p FROM DriverProfileEntity p"
		// + " JOIN p.execution e"
		// + " WHERE e.startTime = :startTime AND p.driverName = :driverName")
		// .setParameter("startTime", startTime).setParameter(
		// "driverName", driverName).getSingleResult();

		// Find the driver profile's profile data.
		// List<ProfileDataEntity> profileData = em
		// .createQuery(
		// "SELECT new
		// net.link.safeonline.performance.entity.ProfileDataEntity("
		// + " d.profile, MIN(d.scenarioStart), ( "
		// + " SELECT new
		// net.link.safeonline.performance.entity.MeasurementEntity("
		// + " m.measurement, AVG(m.duration) "
		// + " ) FROM d.measurements m GROUP BY m.measurement"
		// + " ) "
		// + " ) FROM ProfileDataEntity d "
		// + " WHERE d.profile = :profile")
		// .setParameter("profile", profile).getResultList();

		// Try with just measuremententity.
		List<MeasurementEntity> measurements = em
				.createQuery(
						"SELECT new net.link.safeonline.performance.entity.MeasurementEntity("
								+ "                m.measurement, AVG(m.duration)"
								+ "            ) FROM MeasurementEntity m WHERE m.id = 1")
				.getResultList();

		System.out.println(measurements);
	}
}
