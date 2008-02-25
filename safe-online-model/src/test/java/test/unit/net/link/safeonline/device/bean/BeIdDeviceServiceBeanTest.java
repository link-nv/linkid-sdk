package test.unit.net.link.safeonline.device.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.service.bean.RegistrationStatement;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.bean.BeIdDeviceServiceBean;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.UserRegistrationManager;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.reg.RegistrationStatementFactory;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import test.unit.net.link.safeonline.authentication.service.bean.SoftwareSmartCard;

public class BeIdDeviceServiceBeanTest extends TestCase {

	private BeIdDeviceServiceBean testedInstance;

	private Object[] mockObjects;

	private SecurityAuditLogger mockSecurityAuditLogger;

	private SubjectIdentifierDAO mockSubjectIdentifierDAO;

	private PkiProviderManager mockPkiProviderManager;

	private PkiValidator mockPkiValidator;

	private UserRegistrationManager mockUserRegistrationManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new BeIdDeviceServiceBean();

		this.mockSubjectIdentifierDAO = createMock(SubjectIdentifierDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectIdentifierDAO);

		this.mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSecurityAuditLogger);

		this.mockPkiProviderManager = createMock(PkiProviderManager.class);
		EJBTestUtils.inject(this.testedInstance, this.mockPkiProviderManager);

		this.mockPkiValidator = createMock(PkiValidator.class);
		EJBTestUtils.inject(this.testedInstance, this.mockPkiValidator);

		this.mockUserRegistrationManager = createMock(UserRegistrationManager.class);
		EJBTestUtils.inject(this.testedInstance,
				this.mockUserRegistrationManager);

		EJBTestUtils.init(this.testedInstance);

		this.mockObjects = new Object[] { this.mockSubjectIdentifierDAO,
				this.mockSecurityAuditLogger, this.mockPkiProviderManager,
				this.mockPkiValidator, this.mockUserRegistrationManager };
	}

	public void testRegisterAndAuthenticate() throws Exception {
		// setup
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

		byte[] registrationStatementData = RegistrationStatementFactory
				.createRegistrationStatement(login, sessionId, applicationId,
						smartCard);
		RegistrationStatement registrationStatement = new RegistrationStatement(
				registrationStatementData);

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
						identifier)).andStubReturn(null);
		expect(this.mockUserRegistrationManager.registerUser(login))
				.andStubReturn(subject);
		expect(
				this.mockSubjectIdentifierDAO.findSubject(identifierDomain,
						identifier)).andStubReturn(subject);
		this.mockSubjectIdentifierDAO.addSubjectIdentifier(identifierDomain,
				identifier, subject);
		mockPkiProvider.storeAdditionalAttributes(subject, cert);

		// prepare
		replay(this.mockObjects);
		replay(mockPkiProvider);

		// operate
		SubjectEntity resultSubject = this.testedInstance
				.registerAndAuthenticate(sessionId, login,
						registrationStatement);
		// verify
		verify(this.mockObjects);
		verify(mockPkiProvider);
		assertNotNull(resultSubject);
	}
}
