package test.integ.net.link.safeonline.performance;

import java.util.Date;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
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
import net.link.safeonline.performance.service.bean.AbstractProfilingServiceBean;
import net.link.safeonline.performance.service.bean.DriverExceptionServiceBean;
import net.link.safeonline.performance.service.bean.DriverProfileServiceBean;
import net.link.safeonline.performance.service.bean.ExecutionServiceBean;
import net.link.safeonline.performance.service.bean.ProfileDataServiceBean;
import net.link.safeonline.performance.service.bean.ScenarioTimingServiceBean;
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

    protected final Class<?>[]       entities = new Class[] { ScenarioTimingEntity.class, ExecutionEntity.class,
            DriverProfileEntity.class, DriverExceptionEntity.class, ProfileDataEntity.class, MeasurementEntity.class };

    protected ExecutionService       executionService;
    protected ProfileDataService     profileDataService;
    protected DriverExceptionService driverExceptionService;
    protected DriverProfileService   driverProfileService;
    protected ScenarioTimingService  scenarioTimingService;

    protected EntityManager          em;
    protected EntityTestManager      entityTestManager;

    {
        configure();

        this.entityTestManager = new EntityTestManager();

        try {
            this.entityTestManager.configureMySql(this.DB_HOST, this.DB_PORT, this.DB_NAME, this.DB_USER, this.DB_PASS,
                    this.SHOW_SQL);
            this.entityTestManager.setUp(this.entities);

            this.em = this.entityTestManager.getEntityManager();
            AbstractProfilingServiceBean.setDefaultEntityManager(this.em);

            this.executionService = new ExecutionServiceBean();
            this.profileDataService = new ProfileDataServiceBean();
            this.driverExceptionService = new DriverExceptionServiceBean();
            this.driverProfileService = new DriverProfileServiceBean();
            this.scenarioTimingService = new ScenarioTimingServiceBean();
        }

        catch (Exception e) {
            this.LOG.fatal("JPA annotations incorrect: " + e.getMessage(), e);
            throw new RuntimeException("JPA annotations incorrect: " + e.getMessage(), e);
        }
    }


    /**
     * Get the most recent execution.
     */
    public ExecutionEntity getLatestExecution() {

        Date executionId = new TreeSet<Date>(this.executionService.getExecutions()).last();

        return this.executionService.getExecution(executionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {

        if (this.entityTestManager.getEntityManager() != null) {
            this.entityTestManager.tearDown();
        }
    }

    protected void configure() {

    }
}
