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
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import net.link.safeonline.model.performance.PerformanceService;
import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.performance.service.bean.DriverExceptionServiceBean;
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
public class PerformanceDriverTest {

	static final Log LOG = LogFactory.getLog(PerformanceDriverTest.class);

	private static final String OLAS_HOSTNAME = "sebeco-dev-10:8443";
	// private static final String OLAS_HOSTNAME = "localhost:8443";

	private static final String testApplicationName = "performance-application";
	private static final String testUsername = "performance";
	private static final String testPassword = "performance";
	private static PrivateKeyEntry testApplicationKey;

	static {

		Hashtable<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "jnp://" + OLAS_HOSTNAME
				+ ":1099");
		try {
			PerformanceService service = (PerformanceService) new InitialContext(
					environment).lookup(PerformanceService.BINDING);
			testApplicationKey = new KeyStore.PrivateKeyEntry(service
					.getPrivateKey(), new Certificate[] { service
					.getCertificate() });
		} catch (Exception e) {
			LOG.error("application keys unavailable; will try local keystore.",
					e);
			testApplicationKey = PerformanceKeyStoreUtils.getPrivateKeyEntry();
		}
	}

	private AttribDriver attribDriver;
	private AuthDriver authDriver;
	private IdMappingDriver idDriver;

	private EntityTestManager entityTestManager;

	private ExecutionService executionService;
	private ProfileDataService profileDataService;
	private DriverExceptionService driverExceptionService;

	@Before
	public void setUp() {

		this.entityTestManager = new EntityTestManager();

		try {
			this.entityTestManager.setUp(DriverExceptionEntity.class,
					DriverProfileEntity.class, ExecutionEntity.class,
					MeasurementEntity.class, ProfileDataEntity.class,
					AgentTimeEntity.class);

			ProfilingServiceBean.setDefaultEntityManager(this.entityTestManager
					.getEntityManager());

			this.executionService = new ExecutionServiceBean();
			this.profileDataService = new ProfileDataServiceBean();
			this.driverExceptionService = new DriverExceptionServiceBean();

			ExecutionEntity execution = this.executionService.addExecution(
					getClass().getName(), 1, 1, new Date(), 1l, OLAS_HOSTNAME);
			AgentTimeEntity agentTime = this.executionService.start(execution);

			this.idDriver = new IdMappingDriver(execution, agentTime);
			this.attribDriver = new AttribDriver(execution, agentTime);
			this.authDriver = new AuthDriver(execution, agentTime);
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
	public void testAttrib() throws Exception {

		// User needs to authenticate before we can get to the attributes.
		String uuid = this.authDriver.login(testApplicationKey,
				testApplicationName, testUsername, testPassword);

		getAttributes(testApplicationKey, uuid);
	}

	@Test
	public void testLogin() throws Exception {

		login(testApplicationKey, testApplicationName, testUsername,
				testPassword);
	}

	@Test
	public void testMapping() throws Exception {

		getUserId(testApplicationKey, testUsername);
	}

	private Map<String, Object> getAttributes(PrivateKeyEntry application,
			String uuid) throws Exception {

		// Get attributes for given UUID.
		Map<String, Object> attributes = this.attribDriver.getAttributes(
				application, uuid);

		// State assertions.
		assertProfile(this.attribDriver.getProfile());
		assertTrue(attributes != null && attributes.isEmpty());

		return attributes;

	}

	/**
	 * Get the UUID of the given username for the given application.
	 */
	private String getUserId(PrivateKeyEntry application, String username)
			throws Exception {

		String uuid = this.idDriver.getUserId(application, username);

		// State assertions.
		assertProfile(this.idDriver.getProfile());
		assertTrue("No UUID returned.", uuid != null && uuid.length() > 0);

		return uuid;
	}

	/**
	 * Log the given username in using the given password for the given
	 * application and retrieve the UUID for the user.
	 *
	 * @param testPass2
	 * @param applicationKey2
	 */
	private String login(PrivateKeyEntry applicationKey,
			String applicationName, String username, String password)
			throws Exception {

		// Authenticate User.
		String uuid = this.authDriver.login(applicationKey, applicationName,
				username, password);

		// State assertions.
		assertProfile(this.authDriver.getProfile());
		assertTrue("No UUID returned.", uuid != null && uuid.length() > 0);

		return uuid;

	}

	private void assertProfile(DriverProfileEntity profile) {

		Set<DriverExceptionEntity> errors = this.driverExceptionService
				.getProfileErrors(profile);
		for (DriverExceptionEntity error : errors)
			if (error != null)
				System.err.format("At %s the following occured:\n\t%s\n",
						new Date(error.getOccurredTime()), error.getMessage());

		assertTrue("Errors detected.  See stderr.", isEmptyOrOnlyNulls(errors));
		assertFalse("No profiling data gathered.",
				isEmptyOrOnlyNulls(this.profileDataService.getProfileData(
						profile, 1)));
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
