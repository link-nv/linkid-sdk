package test.integ.net.link.safeonline.performance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;
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
public class PerformanceDriverTest {

    static final Log                  LOG                 = LogFactory.getLog(PerformanceDriverTest.class);

    private static final String       OLAS_HOSTNAME       = "sebeco-dev-10";
    // private static final String OLAS_HOSTNAME = "localhost";
    private static final String       OLAS_PORT           = "8080";
    private static final boolean      OLAS_SSL            = false;

    private static final String       testApplicationName = "performance-application";
    private static final String       testUsername        = "performance";
    private static final String       testPassword        = "performance";

    private static PrivateKeyEntry    testApplicationKey;

    static {

        Hashtable<String, String> environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        environment.put(Context.PROVIDER_URL, "jnp://" + OLAS_HOSTNAME + ":1099");
        try {
            PerformanceServiceRemote service = (PerformanceServiceRemote) new InitialContext(environment)
                                                                                                         .lookup(PerformanceServiceRemote.JNDI_BINDING);
            testApplicationKey = new KeyStore.PrivateKeyEntry(service.getPrivateKey(), new Certificate[] { service.getCertificate() });
        } catch (Exception e) {
            LOG.error("application keys unavailable; will try local keystore.", e);
            testApplicationKey = PerformanceKeyStoreUtils.getPrivateKeyEntry();
        }
    }

    private AttribDriver              attribDriver;
    private AuthDriver                authDriver;
    private IdMappingDriver           idDriver;

    private EntityTestManager         entityTestManager;

    private ExecutionService          executionService;
    private ProfileDataService        profileDataService;
    private ScenarioTimingServiceBean scenarioTimingService;
    private DriverExceptionService    driverExceptionService;


    @Before
    public void setUp() {

        entityTestManager = new EntityTestManager();

        try {
            entityTestManager.setUp(DriverExceptionEntity.class, DriverProfileEntity.class, ExecutionEntity.class,
                    MeasurementEntity.class, ProfileDataEntity.class, ScenarioTimingEntity.class);

            AbstractProfilingServiceBean.setDefaultEntityManager(entityTestManager.getEntityManager());

            executionService = new ExecutionServiceBean();
            profileDataService = new ProfileDataServiceBean();
            scenarioTimingService = new ScenarioTimingServiceBean();
            driverExceptionService = new DriverExceptionServiceBean();

            ExecutionEntity execution = executionService.addExecution(getClass().getName(), 1, 1, new Date(), 1l, OLAS_HOSTNAME + ":"
                    + OLAS_PORT, OLAS_SSL);
            ScenarioTimingEntity agentTime = executionService.start(execution);

            idDriver = new IdMappingDriver(execution, agentTime);
            attribDriver = new AttribDriver(execution, agentTime);
            authDriver = new AuthDriver(execution, agentTime);
        }

        catch (Exception e) {
            LOG.fatal("JPA annotations incorrect: " + e.getMessage(), e);
            throw new RuntimeException("JPA annotations incorrect: " + e.getMessage(), e);
        }
    }

    @After
    public void tearDown()
            throws Exception {

        if (entityTestManager.getEntityManager() != null) {
            entityTestManager.tearDown();
        }
    }

    @Test
    public void annotationCorrectness()
            throws Exception {

        assertNotNull("JPA annotations incorrect?", entityTestManager.getEntityManager());
    }

    @Test
    public void testAttrib()
            throws Exception {

        // User needs to authenticate before we can get to the attributes.
        String uuid = authDriver.login(testApplicationKey, testApplicationName, testUsername, testPassword);

        getAttributes(testApplicationKey, uuid);
    }

    @Test
    public void testLogin()
            throws Exception {

        login(testApplicationKey, testApplicationName, testUsername, testPassword);
    }

    @Test
    public void testMapping()
            throws Exception {

        getUserId(testApplicationKey, testUsername);
    }

    private Map<String, Object> getAttributes(PrivateKeyEntry application, String uuid)
            throws Exception {

        // Get attributes for given UUID.
        Map<String, Object> attributes = attribDriver.getAttributes(application, uuid);

        // State assertions.
        assertProfile(attribDriver.getProfile());
        assertTrue(attributes != null && attributes.isEmpty());

        return attributes;

    }

    /**
     * Get the UUID of the given username for the given application.
     */
    private String getUserId(PrivateKeyEntry application, String username)
            throws Exception {

        String uuid = idDriver.getUserId(application, username);

        // State assertions.
        assertProfile(idDriver.getProfile());
        assertTrue("No UUID returned.", uuid != null && uuid.length() > 0);

        return uuid;
    }

    /**
     * Log the given username in using the given password for the given application and retrieve the UUID for the user.
     * 
     * @param testPass2
     * @param applicationKey2
     */
    private String login(PrivateKeyEntry applicationKey, String applicationName, String username, String password)
            throws Exception {

        // Authenticate User.
        String uuid = authDriver.login(applicationKey, applicationName, username, password);

        // State assertions.
        assertProfile(authDriver.getProfile());
        assertTrue("No UUID returned.", uuid != null && uuid.length() > 0);

        return uuid;

    }

    private void assertProfile(DriverProfileEntity profile) {

        List<DriverExceptionEntity> errors = driverExceptionService.getAllProfileErrors(profile);
        for (DriverExceptionEntity error : errors)
            if (error != null) {
                System.err.format("At %s the following occured:\n\t%s\n", new Date(error.getOccurredTime()), error.getMessage());
            }

        assertTrue("Errors detected.  See stderr.", isEmptyOrOnlyNulls(errors));
        assertFalse("No profiling data gathered.", isEmptyOrOnlyNulls(profileDataService.getProfileData(profile,
                scenarioTimingService.getExecutionTimings(profile.getExecution(), 1))));
    }

    private static boolean isEmptyOrOnlyNulls(Collection<?> profileDataOrErrors) {

        if (profileDataOrErrors == null || profileDataOrErrors.isEmpty())
            return true;

        for (Object data : profileDataOrErrors)
            if (null != data)
                return false;

        return true;
    }
}
