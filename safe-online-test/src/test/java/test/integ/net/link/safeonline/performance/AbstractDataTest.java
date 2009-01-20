package test.integ.net.link.safeonline.performance;

import java.util.Date;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author mbillemo
 * 
 */
@SuppressWarnings("unused")
public abstract class AbstractDataTest {

    protected final Log              LOG      = LogFactory.getLog(getClass());

    protected String                 DB_NAME  = "safeonline";
    protected String                 DB_USER  = "safeonline";
    protected String                 DB_PASS  = "safeonline";
    protected String                 DB_HOST  = "sebeco-dev-11";
    protected int                    DB_PORT  = 3306;
    protected boolean                SHOW_SQL = true;

    protected final Class<?>[]       entities = new Class[] { ScenarioTimingEntity.class, ExecutionEntity.class, DriverProfileEntity.class,
            DriverExceptionEntity.class, ProfileDataEntity.class, MeasurementEntity.class };

    protected ExecutionService       executionService;
    protected ProfileDataService     profileDataService;
    protected DriverExceptionService driverExceptionService;
    protected DriverProfileService   driverProfileService;
    protected ScenarioTimingService  scenarioTimingService;

    protected EntityManager          em;
    protected EntityTestManager      entityTestManager;

    {
        configure();

        entityTestManager = new EntityTestManager();

        try {
            entityTestManager.configureMySql(DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS, SHOW_SQL);
            entityTestManager.setUp(entities);

            em = entityTestManager.getEntityManager();
            AbstractProfilingServiceBean.setDefaultEntityManager(em);

            executionService = new ExecutionServiceBean();
            profileDataService = new ProfileDataServiceBean();
            driverExceptionService = new DriverExceptionServiceBean();
            driverProfileService = new DriverProfileServiceBean();
            scenarioTimingService = new ScenarioTimingServiceBean();
        }

        catch (Exception e) {
            LOG.fatal("JPA annotations incorrect: " + e.getMessage(), e);
            throw new RuntimeException("JPA annotations incorrect: " + e.getMessage(), e);
        }
    }


    /**
     * Get the most recent execution.
     */
    public ExecutionEntity getLatestExecution() {

        Date executionId = new TreeSet<Date>(executionService.getExecutions()).last();

        return executionService.getExecution(executionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize()
            throws Throwable {

        if (entityTestManager.getEntityManager() != null) {
            entityTestManager.tearDown();
        }
    }

    protected void configure() {

    }
}
