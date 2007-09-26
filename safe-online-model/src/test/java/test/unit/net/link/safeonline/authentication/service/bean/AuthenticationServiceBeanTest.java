/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticDomain;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticName;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.auth.AuthenticationStatementFactory;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.HistoryInfoType;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.easymock.EasyMock;

public class AuthenticationServiceBeanTest extends TestCase {

	private AuthenticationServiceBean testedInstance;

	private SubjectDAO mockSubjectDAO;

	private ApplicationDAO mockApplicationDAO;

	private SubscriptionDAO mockSubscriptionDAO;

	private HistoryDAO mockHistoryDAO;

	private PasswordManager mockPasswordManager;

	private PkiProviderManager mockPkiProviderManager;

	private PkiValidator mockPkiValidator;

	private Object[] mockObjects;

	private SubjectIdentifierDAO mockSubjectIdentifierDAO;

	private StatisticDAO mockStatisticDAO;

	private StatisticDataPointDAO mockStatisticDataPointDAO;

	private SecurityAuditLogger mockSecurityAuditLogger;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new AuthenticationServiceBean();

		this.mockSubjectDAO = createMock(SubjectDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectDAO);

		this.mockApplicationDAO = createMock(ApplicationDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockApplicationDAO);

		this.mockSubscriptionDAO = createMock(SubscriptionDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubscriptionDAO);

		this.mockHistoryDAO = createMock(HistoryDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockHistoryDAO);

		this.mockPasswordManager = createMock(PasswordManager.class);
		EJBTestUtils.inject(this.testedInstance, this.mockPasswordManager);

		this.mockPkiProviderManager = createMock(PkiProviderManager.class);
		EJBTestUtils.inject(this.testedInstance, this.mockPkiProviderManager);

		this.mockPkiValidator = createMock(PkiValidator.class);
		EJBTestUtils.inject(this.testedInstance, this.mockPkiValidator);

		this.mockSubjectIdentifierDAO = createMock(SubjectIdentifierDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectIdentifierDAO);

		this.mockStatisticDAO = createMock(StatisticDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockStatisticDAO);

		this.mockStatisticDataPointDAO = createMock(StatisticDataPointDAO.class);
		EJBTestUtils
				.inject(this.testedInstance, this.mockStatisticDataPointDAO);

		this.mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSecurityAuditLogger);

		EJBTestUtils.init(this.testedInstance);

		this.mockObjects = new Object[] { this.mockSubjectDAO,
				this.mockApplicationDAO, this.mockSubscriptionDAO,
				this.mockHistoryDAO, this.mockPasswordManager,
				this.mockPkiProviderManager, this.mockPkiValidator,
				this.mockSubjectIdentifierDAO, this.mockStatisticDAO,
				this.mockStatisticDataPointDAO, this.mockSecurityAuditLogger };
	}

	public void testAuthenticate() throws Exception {
		// setup
		String applicationName = "test-application";
		String login = "test-login";
		String password = "test-password";

		// stubs
		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectDAO.getSubject(login)).andStubReturn(subject);

		SubjectEntity adminSubject = new SubjectEntity("admin-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"test-application-owner", adminSubject);

		ApplicationEntity application = new ApplicationEntity(applicationName,
				applicationOwner);
		expect(this.mockApplicationDAO.findApplication(applicationName))
				.andStubReturn(application);

		SubscriptionEntity subscription = new SubscriptionEntity();
		expect(this.mockSubscriptionDAO.findSubscription(subject, application))
				.andStubReturn(subscription);

		expect(this.mockPasswordManager.validatePassword(subject, password))
				.andStubReturn(true);

		StatisticEntity statistic = new StatisticEntity();
		expect(
				this.mockStatisticDAO
						.findOrAddStatisticByNameDomainAndApplication(
								statisticName, statisticDomain, application))
				.andStubReturn(statistic);
		StatisticDataPointEntity dataPoint = new StatisticDataPointEntity();
		expect(
				this.mockStatisticDataPointDAO.findOrAddStatisticDataPoint(
						"Login counter", statistic)).andStubReturn(dataPoint);

		// prepare
		replay(this.mockObjects);

		// operate
		boolean result = this.testedInstance.authenticate(login, password);

		// verify
		verify(this.mockObjects);
		assertTrue(result);
	}

	public void testAuthenticateWithWrongPasswordFails() throws Exception {
		// setup
		String login = "test-login";
		String wrongPassword = "foobar";

		// stubs
		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectDAO.getSubject(login)).andStubReturn(subject);

		expect(
				this.mockPasswordManager.validatePassword(subject,
						wrongPassword)).andStubReturn(false);

		// expectations
		this.mockHistoryDAO.addHistoryEntry((Date) EasyMock.anyObject(),
				EasyMock.eq(subject), (HistoryEventType) EasyMock.anyObject(),
				(HistoryInfoType) EasyMock.anyObject(), (String) EasyMock
						.anyObject(), (String) EasyMock.anyObject());

		this.mockSecurityAuditLogger.addSecurityAudit(
				SecurityThreatType.DECEPTION, login, "incorrect password");

		// prepare
		replay(this.mockObjects);

		// operate
		boolean result = this.testedInstance.authenticate(login, wrongPassword);

		// verify
		verify(this.mockObjects);
		assertFalse(result);
	}

	public void testAuthenticateWithWrongUsernameFails() throws Exception {
		// setup
		String wrongLogin = "foobar-login";
		String password = "test-password";

		// stubs
		expect(this.mockSubjectDAO.getSubject(wrongLogin)).andStubThrow(
				new SubjectNotFoundException());

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.authenticate(wrongLogin, password);
			fail();
		} catch (SubjectNotFoundException e) {
			// expected
		}

		// verify
		verify(this.mockObjects);
	}

	public void testAuthenticateViaAuthenticationStatement() throws Exception {
		// setup
		String sessionId = UUID.randomUUID().toString();
		String applicationId = "test-application-id";
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate cert = PkiTestUtils.generateSelfSignedCertificate(
				keyPair, "CN=Test");
		SmartCard smartCard = new SoftwareSmartCard(keyPair, cert);
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"test-trust-domain", true);
		PkiProvider mockPkiProvider = createMock(PkiProvider.class);
		String identifierDomain = "test-identifier-domain";
		String identifier = "test-identifier";
		String login = "test-subject-login";
		SubjectEntity subject = new SubjectEntity(login);
		ApplicationEntity application = new ApplicationEntity(applicationId,
				null);
		SubscriptionEntity subscription = new SubscriptionEntity();

		byte[] authenticationStatementData = AuthenticationStatementFactory
				.createAuthenticationStatement(sessionId, applicationId,
						smartCard);

		// stubs
		expect(this.mockPkiProviderManager.findPkiProvider(cert))
				.andStubReturn(mockPkiProvider);
		expect(mockPkiProvider.getTrustDomain()).andStubReturn(trustDomain);
		expect(this.mockPkiValidator.validateCertificate(trustDomain, cert))
				.andStubReturn(true);
		expect(mockPkiProvider.getIdentifierDomainName()).andStubReturn(
				identifierDomain);
		expect(mockPkiProvider.getSubjectIdentifier(cert)).andStubReturn(
				identifier);
		expect(
				this.mockSubjectIdentifierDAO.findSubject(identifierDomain,
						identifier)).andStubReturn(subject);
		expect(this.mockApplicationDAO.findApplication(applicationId))
				.andStubReturn(application);
		expect(this.mockSubscriptionDAO.findSubscription(subject, application))
				.andStubReturn(subscription);

		StatisticEntity statistic = new StatisticEntity();
		expect(
				this.mockStatisticDAO
						.findOrAddStatisticByNameDomainAndApplication(
								statisticName, statisticDomain, application))
				.andStubReturn(statistic);
		StatisticDataPointEntity dataPoint = new StatisticDataPointEntity();
		expect(
				this.mockStatisticDataPointDAO.findOrAddStatisticDataPoint(
						"Login counter", statistic)).andStubReturn(dataPoint);

		// prepare
		replay(this.mockObjects);
		replay(mockPkiProvider);

		// operate
		boolean result = this.testedInstance.authenticate(sessionId,
				authenticationStatementData);

		// verify
		verify(this.mockObjects);
		verify(mockPkiProvider);
		assertTrue(result);
	}
}
