package test.integ.net.link.safeonline.performance;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.util.performance.ProfileData;

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

    static final Log          LOG = LogFactory.getLog(PagingTest.class);

    private EntityTestManager entityTestManager;


    @Before
    public void setUp() {

        this.entityTestManager = new EntityTestManager();

        try {
            this.entityTestManager
                    .configureMySql("sebeco-dev-11", 3306, "safeonline", "safeonline", "safeonline", true);
            this.entityTestManager.setUp(ScenarioTimingEntity.class, ExecutionEntity.class, DriverProfileEntity.class,
                    ProfileDataEntity.class, MeasurementEntity.class);
        }

        catch (Exception e) {
            LOG.fatal("JPA annotations incorrect: " + e.getMessage(), e);
            throw new RuntimeException("JPA annotations incorrect: " + e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {

        if (this.entityTestManager.getEntityManager() != null) {
            this.entityTestManager.tearDown();
        }
    }

    @Test
    public void annotationCorrectness() throws Exception {

        assertNotNull("JPA annotations incorrect?", this.entityTestManager.getEntityManager());
    }

    @Test
    public void testPaging() throws Exception {

        EntityManager em = this.entityTestManager.getEntityManager();

        String driverName = "Authentication Driver";
        Date startTime = new Date(1203519790000l);
        int dataPoints = 10;

        // Find the driver profile.
        DriverProfileEntity profile = (DriverProfileEntity) em.createQuery(
                "SELECT p FROM DriverProfileEntity p" + "    JOIN p.execution e" + "    WHERE e.startTime = :startTime"
                        + "        AND p.driverName = :driverName").setParameter("startTime", startTime).setParameter(
                "driverName", driverName).getSingleResult();

        int repeat = 100;
        long start = System.currentTimeMillis();
        for (int i = 0; i < repeat; ++i) {
            testPagingOne(profile, dataPoints);
        }

        Set<ProfileDataEntity> pointData = testPagingOne(profile, dataPoints);

        System.err.println();
        System.err.println("Duration: " + (System.currentTimeMillis() - start) / repeat);
        System.err.println("ProfileDatas: " + pointData.size());
        System.err.println();

        for (ProfileDataEntity d : pointData) {
            System.out.println("ProfileData: " + new Date(d.getScenarioTiming().getStart()));
            for (MeasurementEntity m : d.getMeasurements()) {
                System.out.println("  - " + m.getMeasurement() + " -> " + m.getDuration());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Set<ProfileDataEntity> testPagingOne(DriverProfileEntity profile, int dataPoints) throws Exception {

        EntityManager em = this.entityTestManager.getEntityManager();

        // Find the driver profile's profile data.
        long dataCount = (Long) em.createQuery(
                "SELECT COUNT(d) FROM ProfileDataEntity d" + "    WHERE d.profile = :profile").setParameter("profile",
                profile).getSingleResult();
        int period = (int) Math.ceil((double) dataCount / dataPoints);
        System.err.println("period = dataCount (" + dataCount + ") / dataPoints (" + dataPoints + ") = " + period);

        Query profileDataQuery = em
                .createQuery(
                        "SELECT d FROM ProfileDataEntity d" + "    WHERE d.profile = :profile"
                                + "    ORDER BY d.scenarioStart").setParameter("profile", profile)
                .setMaxResults(period);

        List<ProfileDataEntity> profileData;
        Set<ProfileDataEntity> pointData = new HashSet<ProfileDataEntity>();
        for (int point = 0; (profileData = profileDataQuery.setFirstResult(point * period).getResultList()) != null; ++point) {
            if (profileData.isEmpty()) {
                break;
            }

            Map<String, Long> durations = new HashMap<String, Long>();
            Map<String, Integer> counts = new HashMap<String, Integer>();
            for (ProfileDataEntity d : profileData) {
                for (MeasurementEntity m : d.getMeasurements())
                    if (!ProfileData.REQUEST_START_TIME.equals(m.getMeasurement())) {
                        if (!durations.containsKey(m.getMeasurement())) {
                            durations.put(m.getMeasurement(), 0l);
                            counts.put(m.getMeasurement(), 0);
                        }

                        durations.put(m.getMeasurement(), durations.get(m.getMeasurement()) + m.getDuration());
                        counts.put(m.getMeasurement(), counts.get(m.getMeasurement()) + 1);
                    } else if (!durations.containsKey(m.getMeasurement())) {
                        durations.put(m.getMeasurement(), m.getDuration());
                        counts.put(m.getMeasurement(), 1);
                    }
            }

            ProfileDataEntity data = new ProfileDataEntity(profileData.get(0).getProfile(), profileData.get(0)
                    .getScenarioTiming());
            pointData.add(data);

            Set<MeasurementEntity> measurements = new HashSet<MeasurementEntity>();
            for (String measurement : durations.keySet()) {
                measurements.add(new MeasurementEntity(data, measurement, durations.get(measurement)
                        / counts.get(measurement)));
            }

        }

        return pointData;
    }

    @SuppressWarnings("unchecked")
    public Set<ProfileDataEntity> testPagingTwo(DriverProfileEntity profile, int dataPoints) throws Exception {

        EntityManager em = this.entityTestManager.getEntityManager();

        // Find the driver profile's profile data.
        long dataDuration = (Long) em.createQuery(
                "SELECT MAX(d.scenarioStart) - MIN(d.scenarioStart)" + "    FROM ProfileDataEntity d            "
                        + "    WHERE d.profile = :profile          ").setParameter("profile", profile)
                .getSingleResult();
        long dataStart = (Long) em.createQuery(
                "SELECT MIN(d.scenarioStart)                       " + "    FROM ProfileDataEntity d            "
                        + "    WHERE d.profile = :profile          ").setParameter("profile", profile)
                .getSingleResult();
        int period = (int) Math.ceil((double) dataDuration / dataPoints);
        System.err
                .println("period = dataDuration (" + dataDuration + ") / dataPoints (" + dataPoints + ") = " + period);

        Set<ProfileDataEntity> pointData = new HashSet<ProfileDataEntity>();
        for (long point = 0; point * period < dataDuration; ++point) {

            ProfileDataEntity data = new ProfileDataEntity(profile, new ScenarioTimingEntity(profile.getExecution()));
            pointData.add(data);

            List<MeasurementEntity> measurements = em.createQuery(
                    "SELECT NEW net.link.safeonline.performance.entity.MeasurementEntity("
                            + "        m.measurement, AVG(m.duration)" + "    )                                 "
                            + "    FROM ProfileDataEntity d          " + "        JOIN d.measurements m         "
                            + "    WHERE d.profile = :profile        " + "    AND d.scenarioStart > :start      "
                            + "    AND d.scenarioStart <= :stop      " + "    GROUP BY m.measurement            ")
                    .setParameter("profile", profile).setParameter("start", dataStart + point * period).setParameter(
                            "stop", dataStart + (point + 1) * period).getResultList();

            for (MeasurementEntity m : measurements) {
                m.setProfileData(data);
            }
        }

        return pointData;
    }
}
