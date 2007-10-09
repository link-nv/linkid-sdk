/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getApplicationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getIdentityService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPkiService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubjectService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubscriptionService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUserRegistrationService;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

import javax.naming.InitialContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import test.integ.net.link.safeonline.IntegrationTestUtils;

/**
 * Integration test for SafeOnline Identifier Mapping Web Service.
 * 
 * @author fcorneli
 * 
 */
public class IdentifierMappingWebServiceTest {

	private static final Log LOG = LogFactory
			.getLog(IdentifierMappingWebServiceTest.class);

	private X509Certificate certificate;

	private PrivateKey privateKey;

	@Before
	public void setUp() throws Exception {

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");
		this.privateKey = keyPair.getPrivate();
	}

	@Test
	public void testMap() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		IdentityService identityService = getIdentityService(initialContext);

		String testApplicationName = UUID.randomUUID().toString();

		// operate: register user
		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password);

		// operate: register certificate as application trust point
		PkiService pkiService = getPkiService(initialContext);

		SubjectService subjectService = getSubjectService(initialContext);
		String adminUserId = subjectService.getSubjectFromUserName("admin")
				.getUserId();
		LOG.debug("admin userId: " + adminUserId);

		IntegrationTestUtils.login(adminUserId, "admin");
		pkiService.addTrustPoint(
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
				this.certificate.getEncoded());

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService
				.addApplication(
						testApplicationName,
						null,
						"owner",
						null,
						true,
						this.certificate.getEncoded(),
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										SafeOnlineConstants.NAME_ATTRIBUTE) }));

		// operate: subscribe onto the application and confirm identity usage
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);

		String userId = subjectService.getSubjectFromUserName(login)
				.getUserId();

		IntegrationTestUtils.login(userId, password);
		subscriptionService.subscribe(testApplicationName);
		identityService.confirmIdentity(testApplicationName);

		// operate & verify
		NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl(
				"localhost:8443", this.certificate, this.privateKey);
		String resultUserId = client.getUserId(login);
		LOG.debug("userId: " + resultUserId);
		assertNotNull(resultUserId);
		assertEquals(userId, resultUserId);
	}

	@Test
	public void testPermissionDenied() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		IdentityService identityService = getIdentityService(initialContext);

		String testApplicationName = UUID.randomUUID().toString();

		// operate: register user
		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password);

		// operate: register certificate as application trust point
		PkiService pkiService = getPkiService(initialContext);

		SubjectService subjectService = getSubjectService(initialContext);
		String adminUserId = subjectService.getSubjectFromUserName("admin")
				.getUserId();
		LOG.debug("admin userId: " + adminUserId);

		IntegrationTestUtils.login(adminUserId, "admin");
		pkiService.addTrustPoint(
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
				this.certificate.getEncoded());

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService
				.addApplication(
						testApplicationName,
						null,
						"owner",
						null,
						false,
						this.certificate.getEncoded(),
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										SafeOnlineConstants.NAME_ATTRIBUTE) }));

		// operate: subscribe onto the application and confirm identity usage
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);

		String userId = subjectService.getSubjectFromUserName(login)
				.getUserId();

		IntegrationTestUtils.login(userId, password);
		subscriptionService.subscribe(testApplicationName);
		identityService.confirmIdentity(testApplicationName);

		// operate & verify
		NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl(
				"localhost:8443", this.certificate, this.privateKey);
		try {
			client.getUserId(login);
			fail();
		} catch (RequestDeniedException e) {
			// expected
		}
	}
}
